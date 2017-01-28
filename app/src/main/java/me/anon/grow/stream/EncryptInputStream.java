package me.anon.grow.stream;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import me.anon.grow.helper.EncryptionHelper;

/**
 * Creates a wrapper for input stream to encrypt the incoming bytes. Data is encrypted as it is read from its source
 */
public class EncryptInputStream extends InputStream
{
	private CipherInputStream cis;
	private InputStream sourceStream;

	public EncryptInputStream(String encryptionKey, InputStream source)
	{
		this.sourceStream = source;

		try
		{
			SecretKey secretKey = EncryptionHelper.generateKey(encryptionKey);

			if (secretKey != null)
			{
				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.ENCRYPT_MODE, secretKey);

				cis = new CipherInputStream(sourceStream, cipher);
			}
		}
		catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e)
		{
			e.printStackTrace();
		}
	}

	@Override public int available() throws IOException
	{
		return cis.available();
	}

	@Override public void mark(int readlimit)
	{
		cis.mark(readlimit);
	}

	@Override public boolean markSupported()
	{
		return cis.markSupported();
	}

	@Override public int read(byte[] buffer) throws IOException
	{
		return cis.read(buffer);
	}

	@Override public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException
	{
		return cis.read(buffer, byteOffset, byteCount);
	}

	@Override public synchronized void reset() throws IOException
	{
		cis.reset();
	}

	@Override public long skip(long byteCount) throws IOException
	{
		return cis.skip(byteCount);
	}

	@Override public int read() throws IOException
	{
		return cis.read();
	}

	@Override public void close() throws IOException
	{
		cis.close();
		sourceStream.close();
	}
}
