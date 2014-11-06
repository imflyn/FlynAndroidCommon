package com.flyn.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.Checksum;

public abstract class FileUtil {
	public static void readFromFile(File targetFile, OutputStream output, int cacheBytesLength) throws IOException {
		if ((targetFile == null) || (output == null)) {
			throw new NullPointerException();
		}
		if (cacheBytesLength <= 0) {
			throw new IllegalArgumentException("The parameter of cacheBytesLength should be great than zero.");
		}
		InputStream input = null;
		try {
			input = new FileInputStream(targetFile);
			readAndWrite(input, output, cacheBytesLength);
		} finally {
			if (input != null) {
				input.close();
			}
		}
	}

	public static void writeToFile(InputStream input, File targetFile, int cacheBytesLength) throws IOException {
		if ((input == null) || (targetFile == null)) {
			throw new NullPointerException();
		}
		if (cacheBytesLength <= 0) {
			throw new IllegalArgumentException("The parameter of cacheBytesLength should be great than zero.");
		}
		OutputStream output = null;
		try {
			File parentFile = targetFile.getParentFile();
			if ((!parentFile.exists()) && (!parentFile.mkdirs())) {
				throw new IOException("could not create the path:" + parentFile.getPath());
			}
			output = new FileOutputStream(targetFile);
			readAndWrite(input, output, cacheBytesLength);
		} finally {
			if (output != null) {
				output.close();
			}
		}
	}

	public static void readAndWrite(InputStream input, OutputStream output, int cacheBytesLength) throws IOException {
		if ((input == null) || (output == null)) {
			throw new NullPointerException();
		}
		if (cacheBytesLength <= 0) {
			throw new IllegalArgumentException("The parameter of cacheBytesLength should be great than zero.");
		}
		BufferedInputStream buffInput = new BufferedInputStream(input);
		BufferedOutputStream buffOutput = new BufferedOutputStream(output);
		byte[] b = new byte[cacheBytesLength];
		int len;
		while ((len = buffInput.read(b)) > 0) {
			buffOutput.write(b, 0, len);
		}
		buffOutput.flush();
	}

	public static void delDirectory(File f) throws IOException {
		if (f.isDirectory()) {
			if (f.listFiles().length == 0) {
				if (!f.delete()) {
					throw new IOException("delete failure!");
				}
			} else {
				File[] delFile = f.listFiles();
				int i = delFile.length;
				for (int j = 0; j < i; j++) {
					delDirectory(delFile[j]);
				}
				if (!f.delete()) {
					throw new IOException("delete failure!");
				}
			}
		} else if (!f.delete()) {
			throw new IOException("delete failure!");
		}
	}

	public static List<File> recursionFile(File base, FileFilter filter, boolean listAll) {
		List<File> list = new LinkedList<File>();
		if ((filter == null) || (filter.accept(base))) {
			list.add(base);
			if (!listAll) {
				return list;
			}
		}
		if ((base != null) && (base.isDirectory())) {
			File[] f = base.listFiles();
			for (int i = 0; i < f.length; i++) {
				List<File> subList = recursionFile(f[i], filter, listAll);
				list.addAll(subList);
				if ((!listAll) && (list.size() > 0)) {
					return list;
				}
			}
		}
		return list;
	}

	public static byte[] readBytes(File file) throws IOException {
		FileInputStream is = new FileInputStream(file);
		return IoUtils.readAllBytesAndClose(is);
	}

	public static void writeBytes(File file, byte[] content) throws IOException {
		OutputStream out = new FileOutputStream(file);
		try {
			out.write(content);
		} finally {
			IoUtils.safeClose(out);
		}
	}

	public static String readUtf8(File file) throws IOException {
		return readChars(file, "UTF-8");
	}

	public static String readChars(File file, String charset) throws IOException {
		Reader reader = new InputStreamReader(new FileInputStream(file), charset);
		return IoUtils.readAllCharsAndClose(reader);
	}

	public static void writeUtf8(File file, CharSequence text) throws IOException {
		writeChars(file, "UTF-8", text);
	}

	public static void writeChars(File file, String charset, CharSequence text) throws IOException {
		Writer writer = new OutputStreamWriter(new FileOutputStream(file), charset);
		IoUtils.writeAllCharsAndClose(writer, text);
	}

	/** Copies a file to another location. */
	public static void copyFile(File from, File to) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(from));
		try {
			OutputStream out = new BufferedOutputStream(new FileOutputStream(to));
			try {
				IoUtils.copyAllBytes(in, out);
			} finally {
				IoUtils.safeClose(out);
			}
		} finally {
			IoUtils.safeClose(in);
		}
	}

	/** Copies a file to another location. */
	public static void copyFile(String fromFilename, String toFilename) throws IOException {
		copyFile(new File(fromFilename), new File(toFilename));
	}

	/**
	 * To read an object in a quick & dirty way. Prepare to handle failures when
	 * object serialization changes!
	 */
	public static Object readObject(File file) throws IOException, ClassNotFoundException {
		FileInputStream fileIn = new FileInputStream(file);
		ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(fileIn));
		try {
			return in.readObject();
		} finally {
			IoUtils.safeClose(in);
		}
	}

	/** To store an object in a quick & dirty way. */
	public static void writeObject(File file, Object object) throws IOException {
		FileOutputStream fileOut = new FileOutputStream(file);
		ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(fileOut));
		try {
			out.writeObject(object);
			out.flush();
			// Force sync
			fileOut.getFD().sync();
		} finally {
			IoUtils.safeClose(out);
		}
	}

	/** @return MD5 digest (32 characters). */
	public static String getMd5(File file) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			return IoUtils.getMd5(in);
		} finally {
			IoUtils.safeClose(in);
		}
	}

	/** @return SHA-1 digest (40 characters). */
	public static String getSha1(File file) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			return IoUtils.getSha1(in);
		} finally {
			IoUtils.safeClose(in);
		}
	}

	public static void updateChecksum(File file, Checksum checksum) throws IOException {
		InputStream in = new BufferedInputStream(new FileInputStream(file));
		try {
			IoUtils.updateChecksum(in, checksum);
		} finally {
			IoUtils.safeClose(in);
		}
	}

}