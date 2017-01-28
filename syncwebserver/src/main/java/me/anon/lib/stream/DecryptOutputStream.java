package me.anon.lib.stream;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;

import me.anon.lib.helper.EncryptionHelper;

public class DecryptOutputStream extends OutputStream
{
	private CipherOutputStream cos;
	private OutputStream fos;

	public DecryptOutputStream(String key, OutputStream fos) throws FileNotFoundException
	{
		try
		{
			SecretKey secretKey = EncryptionHelper.generateKey(key);

			if (secretKey != null)
			{
				this.fos = fos;

				Cipher cipher = Cipher.getInstance("AES");
				cipher.init(Cipher.DECRYPT_MODE, secretKey);

				cos = new CipherOutputStream(fos, cipher);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override public void close() throws IOException
	{
		cos.close();
		fos.close();
	}

	@Override public void flush() throws IOException
	{
		cos.flush();
		fos.flush();
	}

	@Override public void write(byte[] buffer) throws IOException
	{
		cos.write(buffer);
	}

	@Override public void write(byte[] buffer, int offset, int count) throws IOException
	{
		cos.write(buffer, offset, count);
	}

	@Override public void write(int oneByte) throws IOException
	{
		cos.write(oneByte);
	}
}
