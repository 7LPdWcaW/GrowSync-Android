package me.anon.grow.sync.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main
{
	private static String outPath = "./sync/";
	private static int port = 8420;

	public static void main(String[] args) throws Exception
	{
		if (args.length > 0)
		{
			for (int index = 0; index < args.length; index++)
			{
				if ("--path".equalsIgnoreCase(args[index]))
				{
					outPath = args[index + 1];
				}
				else if ("--port".equalsIgnoreCase(args[index]))
				{
					port = Integer.parseInt(args[index + 1]);
				}
			}
		}

		new File(outPath).mkdirs();

		HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
		server.createContext("/plants", new HttpHandler()
		{
			@Override public void handle(HttpExchange httpExchange) throws IOException
			{
				if (!httpExchange.getRequestMethod().equalsIgnoreCase("post"))
				{
					respond(httpExchange, 400);
					return;
				}

				FileOutputStream fileOutputStream = new FileOutputStream(new File(outPath, "plants.json"));
				BufferedInputStream inputStream = new BufferedInputStream(httpExchange.getRequestBody(), 8192);
				byte[] buffer = new byte[8192];
				int read = -1;

				while ((read = inputStream.read(buffer)) != -1)
				{
					fileOutputStream.write(buffer, 0, read);
				}

				fileOutputStream.flush();
				fileOutputStream.close();

				respond(httpExchange, 200);
			}
		});

		server.createContext("/image", new HttpHandler()
		{
			private Pattern namePattern = Pattern.compile("[^e]name=\"(.+?)\"");
			private Pattern fileNamePattern = Pattern.compile("filename=\"(.+?)\"");
			private String escapedSeparator = "\\" + System.getProperty("file.separator");

			@Override public void handle(HttpExchange httpExchange) throws IOException
			{
				if (!httpExchange.getRequestMethod().equalsIgnoreCase("post"))
				{
					respond(httpExchange, 400);
					return;
				}

				// multipart/form-data, boundary=AaB03x
				List<String> contentTypes = httpExchange.getRequestHeaders().get("Content-Type");
				String contentType = null;
				String boundaryStr = "";

				if (contentTypes != null && contentTypes.size() > 0)
				{
					contentType = contentTypes.get(0);
					if (contentType.startsWith("multipart/form-data;"))
					{
						String[] params = contentType.split("boundary=");
						boundaryStr = params[params.length - 1];
					}
				}

				if (boundaryStr.equalsIgnoreCase(""))
				{
					respond(httpExchange, 400);
					return;
				}

				BufferedInputStream inputStream = new BufferedInputStream(httpExchange.getRequestBody(), 8192);
				byte[] newLine = "\r\n".getBytes();
				byte[] boundary = boundaryStr.getBytes();
				byte[] endOfPart = ("\r\n--" + boundaryStr).getBytes();
				ArrayList<FileData> files = new ArrayList<FileData>();

				while (true)
				{
					String firstline = new String(readUntil(inputStream, newLine), "utf-8");

					if (firstline.equals("--" + boundaryStr))
					{
						continue;
					}

					FileData current = new FileData();

					// Because APARENTLY, many http clients DONT send the required boundary field inbetween file
					// parts.
					if (firstline.toLowerCase().startsWith("content-disposition"))
					{
						InputStream temp = new ByteArrayInputStream(firstline.getBytes());
						readDisposition(temp, current);
					}
					else
					{
						readDisposition(inputStream, current);
					}

					if (firstline.toLowerCase().startsWith("content-type"))
					{
						InputStream temp = new ByteArrayInputStream(firstline.getBytes());
						readContentType(temp, current);
					}
					else
					{
						readContentType(inputStream, current);
					}

					current.data = readUntil(inputStream, endOfPart);
					files.add(current);

					if (current.data.length >= 0)
					{
						String currentLine = new String(readUntil(inputStream, newLine), "utf-8");

						// Check if this is the last part.
						if (currentLine.equals("--"))
						{
							break;
						}
					}
				}

				// write files to disk
				FileData pathInfo = null, photo = null;
				for (FileData file : files)
				{
					if ("filename".equalsIgnoreCase(file.name))
					{
						pathInfo = file;
					}
					else if ("image".equalsIgnoreCase(file.name))
					{
						photo = file;
					}
				}

				if (pathInfo != null && photo != null)
				{
					File file = new File(outPath, new String(pathInfo.data, "utf-8"));
					new File(file.getParent()).mkdirs();

					FileOutputStream fileOutputStream = new FileOutputStream(file);

					for (byte b : photo.data)
					{
						fileOutputStream.write(b);
					}

					fileOutputStream.flush();
					fileOutputStream.close();
				}

				respond(httpExchange, 200);
			}

			private void readContentType(InputStream is, FileData file) throws IOException
			{
				while (true)
				{
					String currentLine = new String(readUntil(is, "\r\n".getBytes()), "utf-8");

					// Wait for a blank line.
					if (currentLine.equals(""))
					{
						break;
					}

					// Get content type.
					if (currentLine.toLowerCase().startsWith("content-type:"))
					{
						int spaceIndex = currentLine.indexOf(' ');
						if (spaceIndex < 0)
						{
							file.contentType = null;
						}
						else
						{
							file.contentType = currentLine.substring(spaceIndex + 1);
						}
					}
				}
			}

			private void readDisposition(InputStream is, FileData data) throws IOException
			{
				String disposition = new String(readUntil(is, "\r\n".getBytes()), "utf-8");
				Matcher m;

				m = namePattern.matcher(disposition);
				if (m.find())
				{
					data.name = m.group(1);
				}

				m = fileNamePattern.matcher(disposition);

				if (m.find())
				{
					data.fileName = m.group(1);
					int lastSeparator = data.fileName.lastIndexOf(escapedSeparator);
					if (lastSeparator >= 0)
					{
						data.fileName = data.fileName.substring(lastSeparator + escapedSeparator.length());
					}
				}
			}

			private byte[] readUntil(InputStream is, byte[] tail) throws IOException
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				int b = -1;
				byte[] current = tail != null ? new byte[tail.length] : null;
				byte[] data = null;

				while ((b = is.read()) >= 0)
				{
					bos.write(b);
					if (current != null)
					{
						System.arraycopy(current, 1, current, 0, current.length - 1);
						current[current.length - 1] = (byte)b;

						if (Arrays.equals(tail, current))
						{
							byte[] rawData = bos.toByteArray();
							data = new byte[rawData.length - tail.length];
							System.arraycopy(rawData, 0, data, 0, data.length);
							break;
						}
					}
				}

				if (data == null)
				{
					data = bos.toByteArray();
				}

				return data;
			}
		});

		server.setExecutor(null);
		server.start();
	}

	private static void respond(HttpExchange httpExchange, int errorCode)
	{
		try
		{
			httpExchange.sendResponseHeaders(errorCode, 0);
			OutputStream os = httpExchange.getResponseBody();
			os.write(0);
			os.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
}
