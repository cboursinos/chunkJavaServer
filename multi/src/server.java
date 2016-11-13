import java.io.*;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.Scanner;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.sql.Timestamp;
import java.util.Date;

class TCPServer {
	private static String FileName = "";

	public static String directory;

	public static void main(String argv[]) throws Exception {

		System.out.println("Welcome to the stream Server");
		int choice;
		System.out.println("You start the Server for first time? type 1 ");
		System.out.println("You start the Server for second time for the same experiment? type other ");
		Scanner in1 = new Scanner(System.in);
		choice= in1.nextInt();
		System.out.println("Write me the directory of the main file where it is");
		Scanner in3 = new Scanner(System.in);
		directory = in3.next();
		System.out.println("Tell me the time in length of the video in second");
		Scanner in4 = new Scanner(System.in);
		String time = in4.next();

		if (choice == 1){
			String path = directory;
			String fname = " ";
			File folder = new File(path);
			File[] listOfFiles = folder.listFiles();

			for (int i = 0; i < listOfFiles.length; i++) {

				if (listOfFiles[i].isFile()) {
					fname = directory + listOfFiles[i].getName();
					FileName = listOfFiles[i].getName();
					System.out.println(fname);
				}

				System.out.println("Reading the file from the directory");
				File ifile = new File(fname);
				FileInputStream fis;
				String newName;
				FileOutputStream chunk;

				int fileSize = (int) ifile.length();
				System.out.println("File Size  " + fileSize);

				int numberOfChunks;
				System.out.println("Put how many chunks you want");
				Scanner in = new Scanner(System.in);

				numberOfChunks = in.nextInt();
				in.close();

				int Chunk_Size = (fileSize / numberOfChunks);

				System.out.println("Chunk Size in bytes " + Chunk_Size);

				int nChunks = 0, read = 0, readLength = Chunk_Size;
				byte[] byteChunk;
				System.out.println("Splitting the file");
				try {
					fis = new FileInputStream(ifile);

					while (fileSize > 0) {
						if (fileSize <= Chunk_Size) {
							readLength = fileSize;
						}
						byteChunk = new byte[readLength];
						read = fis.read(byteChunk, 0, readLength);
						fileSize -= read;
						assert (read == byteChunk.length);
						nChunks++;

						newName = String.format("%s.part%06d", fname, nChunks - 1);
						System.out.println("the " + nChunks + "file " + newName);
						chunk = new FileOutputStream(new File(newName));
						chunk.write(byteChunk);
						chunk.flush();
						chunk.close();
						byteChunk = null;
						chunk = null;
					}
					fis.close();
					fis = null;

					File folder1 = new File(path);
					File[] listOfFiles1 = folder1.listFiles();
					int withoutbigfile = listOfFiles1.length - 1;
					System.out.println("chunks cutted" + withoutbigfile);
					String with = Integer.toString(withoutbigfile);
					String Chunk_SizeStr = Integer.toString(Chunk_Size);

					try {

						DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
						DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

						// root elements
						Document doc = docBuilder.newDocument();
						Element rootElement = doc.createElement("file");
						doc.appendChild(rootElement);

						// staff elements
						Element staff = doc.createElement("Staff");
						rootElement.appendChild(staff);

						// firstname elements
						Element firstname = doc.createElement("numbofchunk");
						firstname.appendChild(doc.createTextNode(with));
						staff.appendChild(firstname);

						// lastname elements
						Element lastname = doc.createElement("name");
						lastname.appendChild(doc.createTextNode(FileName));
						staff.appendChild(lastname);
						//
						// nickname elements
						Element nickname = doc.createElement("lengthOfChunk");
						nickname.appendChild(doc.createTextNode(Chunk_SizeStr));
						staff.appendChild(nickname);
						//
						Element testname = doc.createElement("secondVideo");
						testname.appendChild(doc.createTextNode(time));
						staff.appendChild(testname);

						// write the content into xml file
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(doc);
						StreamResult result = new StreamResult(new File("C:/pao/new2/metadata.xml"));

						// Output to console for testing
						// StreamResult result = new StreamResult(System.out);

						transformer.transform(source, result);

						System.out.println("File saved!");

					} catch (ParserConfigurationException pce) {
						pce.printStackTrace();
					} catch (TransformerException tfe) {
						tfe.printStackTrace();
					}
				}
				finally {
				}
			}
		}

		System.out.println("listening to Clients");
		ServerSocket welcomeSocket = new ServerSocket(3248,200);

		while (true) {
			Socket connectionSocket = welcomeSocket.accept();
			if (connectionSocket != null) {
				Client client = new Client(connectionSocket);
				client.start();

			}
		}
	}
}

class Client extends Thread {
	private Socket connectionSocket;

	public Client(Socket c) throws IOException {
		connectionSocket = c;
	}

	public void run() {

		String path = "";
		File folder = new File(path);
		File[] listOfFiles = folder.listFiles();


		try {
			String fileToSendStr = readFile();

			File fileToSend = null;
			for (File f : listOfFiles) {
				if (f.getName().equals(fileToSendStr)) {
					fileToSend = f;
					break;
				}
			}
			System.out.println("Sending the chunk to Client " + fileToSendStr + "to the client: " +connectionSocket.getRemoteSocketAddress().toString());
			java.util.Date date= new java.util.Date();
			System.out.println(new Timestamp(date.getTime()));
			long length = fileToSend.length();
			byte [] longBytes = new byte[8];
			ByteBuffer bbuffer = ByteBuffer.wrap(longBytes);
			bbuffer.putLong(length);
			connectionSocket.getOutputStream().write(longBytes);

			BufferedOutputStream bout = new BufferedOutputStream(connectionSocket.getOutputStream());
			BufferedInputStream bain = new BufferedInputStream(new FileInputStream(fileToSend));

			byte buffer [] = new byte [1024];
			int i = 0;
			while((i = bain.read(buffer, 0, 1024)) >= 0){
				bout.write(buffer, 0, i);
			}
			System.out.println("chunk sended");
			java.util.Date date1= new java.util.Date();
			System.out.println(new Timestamp(date1.getTime()));
			bout.close();
			bain.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private String readFile() throws IOException {

		BufferedReader r = new BufferedReader(new InputStreamReader(
				connectionSocket.getInputStream()));
		return r.readLine();

	}
}