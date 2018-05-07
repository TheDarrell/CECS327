import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;

/*
 * Find server with matching Pastry ID in classes' pastry routing
 * server implementation. Record # of hops to find each destination
 */
public class Assn10Client {
	private final static int MAX = 10;
	private final static int NUM_TIMES = 1000;
	public static void main(String args[]){
		// args give message contents and server hostname
		ArrayList<Integer> numHops = new ArrayList<Integer> ();
		
		for(int i = 0; i < NUM_TIMES; i++)
		{
			//Generate random 4 digit quaternary Pastry ID
			String destination = randomQuat();
			//Go to my routing table first
			String IP = args[0];
			
			//Initialize
			int hops = 0;
			boolean found = false;
			DatagramSocket aSocket = null;
			//Console Output
			System.out.println((i+1) + ".");
			System.out.println("Destination: " + destination);
			
			//Keep going until it finds the server with matching Pastry ID
			//or detects an infinite due to bad implementation of other servers
			while(!found && hops < MAX)
			{
				try {
					//Send request
					aSocket = new DatagramSocket();
					aSocket.setSoTimeout(3000);
					byte [] m = destination.getBytes();
					InetAddress aHost = InetAddress.getByName(IP);
					int serverPort = 32710;
					DatagramPacket request = new DatagramPacket(m, m.length, aHost, serverPort);
					aSocket.send(request);
					
					//Get reply
					byte[] buffer = new byte[1000];
					DatagramPacket reply = new DatagramPacket(buffer, buffer.length);
					aSocket.receive(reply);
					System.out.println("Reply: " + new String(reply.getData()));
					String replyMess = new String(reply.getData()).trim();
					String[] messageParts = new String[1];
					messageParts[0] = replyMess;
					
					//Specified Format: "NodeID:IP"
					if(replyMess.contains(":"))
					{
						messageParts = replyMess.split(":");
					}else if(replyMess.contains("-"))//Some classmates use this instead
					{
						messageParts = replyMess.split("-");
					}
					
					//Special Case for this one person's strange & specific format
					//"Pastry ID: xxxx
					// Address: yyyyyyy"
					if(replyMess.contains("Pastry ID:"))
					{
						replyMess = replyMess.replace("\n", "");
						replyMess = replyMess.replace("Pastry ID:", "").trim();
						messageParts = replyMess.split("Address:");
					}
					
					String node = messageParts[0].trim();
					//Finds destination if the node ID matches with Pastry ID
					//or it returns null, which means the destination does not exist
					if(node.equals(destination) || replyMess.toLowerCase().contains("null"))
					{
						hops++;
						numHops.add(hops);
						System.out.println("Found Destination!");
						found = true;
					}else
					{
						//If valid format since message should have 2 entries, go to next IP.
						//Otherwise, skip to next number.
						if(messageParts.length == 2)
						{
							hops++;
							IP = messageParts[1].trim();
						}else
						{
							found = true;
						}						
					}
				} catch (SocketException e){
					System.out.println("Socket: " + e.getMessage());
					found = true;
				} catch (IOException e){
					System.out.println("IO: " + e.getMessage());
					found = true;
				} finally { 
					if(aSocket != null) aSocket.close();
				}
			}
			//Prints if detects infinite
			if(hops >= MAX)
			{
				System.out.println("Infinite detected!");
			}
			System.out.println(" ");
		}
		
		//Print out results
		System.out.println("Success: " + numHops.size() + "/" + NUM_TIMES);
		System.out.println("Hops: Prob:    Occur: ");
		System.out.println("----------------------------------------");
		for(int i = 0; i < 7; i++)
		{
			int occur = getNumOfElementInList(numHops, i);
			
			double prob = (double) occur/numHops.size();
			System.out.println(i + "     " + String.format("%.3f",prob) + "    " + occur);
		}
		
	}
	//Generate random Quaternary(0-3) 4 digit number
	private static String randomQuat()
	{
		String quat = "";
		for(int i = 0; i < 4; i++)
		{
			int digit = (int)(Math.random() * 4);
			quat = quat + Integer.toString(digit);
		}
		return quat;
	}
	//Returns the number of occurrences of specified int in the list
	public static int getNumOfElementInList(ArrayList<Integer> myList, int myElement){
		   int count = 0;
		   for(int element: myList){
		      if(element == myElement)
		         count++;
		   }
		   return count;
		}
}
