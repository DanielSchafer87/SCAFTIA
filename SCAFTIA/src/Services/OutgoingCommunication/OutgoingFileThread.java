package Services.OutgoingCommunication;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;

public class OutgoingFileThread extends Thread {
    String neighbor;
    OutputStream os = null;
    private byte[] encryptedFileBytes;

    public OutgoingFileThread(String neighbor, byte[] encryptedFileBytes){
        this.neighbor = neighbor;
        this.encryptedFileBytes = encryptedFileBytes;
    }

    @Override
    public void run() {
        Socket client = null;
        try {
             // now open a conversation with the neighbor
             String ip = neighbor.split(":")[0].trim().replace("/","");
             InetAddress neighborAddress = InetAddress.getByName(ip);
             int port = Integer.parseInt(neighbor.split(":")[1].trim());
             // build a socket and it connects automatically
             client = new Socket(neighborAddress, port);

             os = client.getOutputStream();

            //Read File Contents into contents array
            byte[] contents;
            int fileLength = encryptedFileBytes.length;
            int current = 0;

            System.out.println("start sending file");
            //send the file in chunks.
            while(current!=fileLength){
                int size = 100;
                if(fileLength - current >= size)
                    current += size;
                else{
                    size = (int)(fileLength - current);
                    current = fileLength;
                }
                contents = new byte[size];
                System.arraycopy(encryptedFileBytes,(current-size),contents,0,size);
                os.write(contents);
                System.out.print("Sending file ... "+(current*100)/fileLength+"% complete!\n");
            }

            os.flush();
            //File transfer done.
            //Close the socket connection!
            //Close all streams.
            client.close();
        } catch (IOException e){
              //e.printStackTrace();
            }

    }
}
