import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.io.*;
public class Assn9Server{
	public static void main(String args[]){
		DatagramSocket aSocket = null;
		try{
			aSocket = new DatagramSocket(32710);
			byte[] buffer = new byte[1000];
			while(true){
				DatagramPacket request = new DatagramPacket(buffer, buffer.length);
				aSocket.receive(request);
				DatagramPacket reply = null;
				if(isQuaternary(new String(request.getData())))
				{
					String pastry = new String(request.getData()).trim();
					HashMap<String, String> leafSet = new HashMap<String, String>();
					HashMap<String, HashMap<String, String>> routing = createRouting();
					
					//Pastry ID: 2102
					String current = "2102";
					String currentIP = "54.219.136.134";
					
					//Difference of Pastry & Current
					int pDiff = Math.abs(Integer.parseInt(current) - Integer.parseInt(pastry));
					
					//Make leafSet
					leafSet.put("2022", "18.218.254.202");
					leafSet.put("2101", "54.177.110.97");
					leafSet.put("2133", "54.183.67.190");
					leafSet.put("2233", "52.14.38.156");
					
					//Check if pastry within the leafSet
					int pastryNum = Integer.parseInt(pastry);
					if(pastryNum >= Integer.parseInt("2022")
							&& pastryNum <= Integer.parseInt("2233"))
					{
						//Return current node if pastry is the same as current node
						if(pastryNum == Integer.parseInt(current))
						{
							//Make reply
							String s = current + ":" + currentIP;
							request.setData(s.getBytes());
							request.setLength(s.length());
							reply = new DatagramPacket(request.getData(),
								request.getLength(), request.getAddress(), request.getPort());
						}else if(isNodeNull(pastry, routing, leafSet))
						{
							//Check if a node within range of leafSet is null
							//Make reply
							String s = pastry + ":NULL";
							request.setData(s.getBytes());
							request.setLength(s.length());
							reply = new DatagramPacket(request.getData(),
								request.getLength(), request.getAddress(), request.getPort());
						}else
						{
							//Return node that is closest to pastry
							ArrayList<String> diff = new ArrayList<String>(); 
							diff.addAll(leafSet.keySet());
							int index = diff.lastIndexOf(findMin(diff, pastryNum));
							
							//Make reply
							String s = diff.get(index) + ":" + leafSet.get(diff.get(index));
							request.setData(s.getBytes());
							request.setLength(s.length());
							reply = new DatagramPacket(request.getData(),
								request.getLength(), request.getAddress(), request.getPort());
						}
					}else
					{
						//Find number of matched digits
						String matchDigit = findMatchDigit(pastry, current);
						//Go to the row that correspond to the matched digits
						HashMap<String,String> row = routing.get(matchDigit);
						
						//Used to go to corresponding element in row
						char nextDigit;
						
						if(pastry.length() == matchDigit.length())
						{
							//If there are no more digits after the matched digits in pastry, then
							//go to previous row.
							String prevKey = matchDigit.substring(0, matchDigit.length()-1);
							if(prevKey.length() == 0)
							{
								prevKey = " ";
							}
							row = routing.get(prevKey);
							nextDigit = pastry.charAt(matchDigit.length()-1);
							//Update matchDigit
							matchDigit = prevKey;
						}
						else
						{
							nextDigit = pastry.charAt(matchDigit.trim().length());
						}
						
						ArrayList<String> element = new ArrayList<String> ();
						element.addAll(row.keySet());
						
						//Find the node of the row that starts with the next digit of pastry
						String key = findNode(element, nextDigit);
						//Full ID of Node
						String node = (matchDigit + key).trim();
						//Get the IP of that node
						String nextIP = row.get(key);
						
						//Find a different node if it is NULL
						if(nextIP.equals("NULL"))
						{
							//Node is NULL if the node ID is the same as the pastry ID, since the node you
							//are looking for does not exist
							if(!node.equals(pastry))
							{
								//Check the next node in the row
								char nextQuad = nextQuadDigit(nextDigit);
								String possKey = findNode(element, nextQuad);
								String possNode = (matchDigit + possKey).trim();
								int difference = Math.abs(Integer.parseInt(possNode) - Integer.parseInt(current));
								//Check if the new node is closer to its destination (pastry)
								if(difference < pDiff)
								{
									node = possNode;
									nextIP = row.get(possKey);
								}else
								{
									//Check the previous node in the row
									char prevQuad = prevQuadDigit(nextDigit);
									possKey = findNode(element, prevQuad);
									possNode = (matchDigit + possKey).trim();
									difference = Math.abs(Integer.parseInt(possNode) - Integer.parseInt(current));
									//Check if the new node is closer to its destination (pastry)
									if(difference < pDiff)
									{
										node = possNode;
										nextIP = row.get(possKey);
									}
								}
								//If both the prev & next node are not closer to its destination (pastry),
								//the destination most likely not exist
							}
						}
						
						//Make reply
						String s = node + ":" + nextIP;
						request.setData(s.getBytes());
						request.setLength(s.length());
						reply = new DatagramPacket(request.getData(),
							request.getLength(), request.getAddress(), request.getPort());
						
					}
				}else
				{
					String s = "INVALID REQUEST!";
					request.setData(s.getBytes());
					request.setLength(s.length());
					reply = new DatagramPacket(request.getData(),
						request.getLength(), request.getAddress(), request.getPort());
				}
				
				aSocket.send(reply);
				//Reset buffer so digits from previous requests don't appear in the next request
				buffer = new byte[1000];
			}
		} catch (SocketException e){System.out.println("Socket: " + e.getMessage());
		} catch (IOException e) {System.out.println("IO: " + e.getMessage());
		} finally {if (aSocket != null) aSocket.close();}
	}
	
	private static boolean isQuaternary (String num)
	{
		num = num.trim();
		if(num.length() > 0 && num.length() <= 4)
		{
			for(int i = 0; i < num.length(); i++)
			{
				char c = num.charAt(i);
				if(!(c == '0' || c == '1' || c == '2' || c == '3'))
				{
					return false;
				}
			}
			return true;
		}else
		{
			return false;
		}
	}
	
	private static HashMap<String, HashMap<String, String>> createRouting()
	{
		//Pastry ID: 2102
		
		HashMap<String, HashMap<String, String>> routing = new HashMap<String, HashMap<String,String>>();
		//Row 1
		HashMap<String, String> row1 = new HashMap<String,String>();
		row1.put("0103", "54.172.160.26");
		row1.put("1131", "52.10.42.73");
		row1.put("2102", "54.219.136.134");
		row1.put("3122", "54.67.80.237");
		routing.put(" ", row1);
		
		//Row 2
		HashMap<String, String> row2 = new HashMap<String,String>();
		row2.put("020", "18.219.20.177");
		row2.put("102", "54.219.136.134");
		row2.put("23X", "NULL");
		row2.put("322", "54.219.171.92");
		routing.put("2", row2);
		
		//Row 3
		HashMap<String, String> row3 = new HashMap<String,String>();
		row3.put("02", "54.219.136.134");
		row3.put("1X", "NULL");
		row3.put("2X", "NULL");
		row3.put("3X", "NULL");
		routing.put("21", row3);
		
		//Row 4
		HashMap<String, String> row4 = new HashMap<String,String>();
		row4.put("0", "NULL");
		row4.put("1", "54.177.110.97");
		row4.put("2", "54.219.136.134");
		row4.put("3", "NULL");
		
		routing.put("210", row4);
		
		return routing;
	}
	
	private static String findMin(ArrayList<String> i, int pastry)
	{
		int min = Integer.MAX_VALUE;
		String closest = "";
		for(String e : i)
		{
			if(min > Math.abs(pastry - Integer.parseInt(e)))
			{
				min = Math.abs(pastry - Integer.parseInt(e));
				closest = e;
			}
		}
		return closest;
	}
	
	private static String findMatchDigit(String s1, String s2)
	{
		String shorter;
		if(s1.length() >= s2.length())
		{
			shorter = s2;
		}else
		{
			shorter = s1;
		}
		String matchDigit = " ";
		for(int i = 0; i < shorter.length(); i++)
		{
			if(s1.charAt(i) == s2.charAt(i))
			{
				matchDigit = (matchDigit + s1.charAt(i)).trim();
			}else
			{
				break;
			}
		}
		
		return matchDigit;
	}
	
	private static String findNode(ArrayList<String> element, char nextDigit)
	{
		for(String s : element)
		{
			if(s.charAt(0) == nextDigit)
			{
				return s;
			}
		}
		//Code should not get past foreach loop
		System.out.println("Error!");
		return "";
	}
	
	//0->1->2->3->0
	private static char nextQuadDigit(char d)
	{
		if(d == '3')
		{
			return '0';
		}else
		{
			d++;
			return d;
		}
	}
	
	//3<-0<-1<-2<-3
	private static char prevQuadDigit(char d)
	{
		if(d == '0')
		{
			return '3';
		}else
		{
			d--;
			return d;
		}
	}
	
	//Check if pastry is the same as one of the nodes that is null in routing table 
	private static boolean isNodeNull(String pastry, HashMap<String, HashMap<String,String>> routing, HashMap<String,String> leafSet)
	{
		ArrayList<String> row = new ArrayList<String> ();
		row.addAll(routing.keySet());
		boolean isNull = false;
		for(String r: row)
		{
			HashMap<String, String> rowMap = routing.get(r);
			ArrayList<String> element = new ArrayList<String> ();
			element.addAll(rowMap.keySet());
			for(String e: element)
			{
				ArrayList<String> leaf = new ArrayList<String> ();
				leaf.addAll(leafSet.keySet());
				boolean notNull = false;
				//If in leafSet, do not return NULL
				for(String l: leaf)
				{
					if(l.equals(pastry))
					{
						notNull = true;
					}
				}
				if(!notNull)
				{
					if(e.contains("X"))
					{
						if(pastry.substring(0, pastry.length()-1)
							.equals((r+(e.replace('X', ' ').trim()))))
							{
								isNull = true;
							}
					}
					else if(pastry.equals((r+e).trim()))
					{
						isNull = true;
					}
				}
				
			}
		}
		return isNull;
	}
}