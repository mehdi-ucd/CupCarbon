/*----------------------------------------------------------------------------------------------------------------
 * CupCarbon: OSM based Wireless Sensor Network design and simulation tool
 * www.cupcarbon.com
 * ----------------------------------------------------------------------------------------------------------------
 * Copyright (C) 2013 Ahcene Bounceur
 * ----------------------------------------------------------------------------------------------------------------
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *----------------------------------------------------------------------------------------------------------------*/

package device;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import map.Layer;
import solver.SensorSetCover;
import utilities.MapCalc;

/**
 * @author Ahcene Bounceur
 * @author Kamal Mehdi
 * @author Lounis Massinissa
 * @version 1.0
 */
public class DeviceList {

	private static List<Device> nodes = new ArrayList<Device>();
	private boolean liens = true;
	private boolean liensDetection = true;	
	private boolean displayConnectionDistance = false ;
	private static int size = 0;
	private LinkedList<Point []> linksCoord = new LinkedList<Point []>();

	/**
	 * 
	 */
	public DeviceList() {
		//Thread th = new Thread(this);
		//th.start();
	}

	/**
	 * @return the nodes
	 */
	public static List<Device> getNodes() {
		return nodes ;
	}
	
	/**
	 * @param fileName
	 */
	public static void save(String fileName) {		
		try {
			PrintStream fos = new PrintStream(new FileOutputStream(fileName));
			Device node;
			for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
				node = iterator.next();
				System.out.println(node.getGPSFileName());
				fos.print(node.getType());
				fos.print(" " + node.getId());
				fos.print(" " + node.getUserId());
				fos.print(" " + node.getX());
				fos.print(" " + node.getY());
				fos.print(" " + node.getRadius());
				if (node.getType() == 1 || node.getType() == 4 || node.getType() == 5 || node.getType() == 7)
					fos.print(" " + node.getRadioRadius());
				if (node.getType() == 1)
					fos.print(" " + node.getCaptureUnitRadius());
				if (node.getType() == 1 || node.getType() == 3 || node.getType() == 6 || node.getType() == 7)
					fos.print(" " + ((node.getGPSFileName()=="")?"#":node.getGPSFileName()));
				if (node.getType() == 1)
					fos.print(" " + ((node.getCOMFileName()=="")?"#":node.getCOMFileName()));
				
				fos.println();
				
			}
			fos.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param fileName
	 */
	public static void open(String fileName) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(fileName));
			String line;
			String[] str;
			while ((line = br.readLine()) != null) {
				str = line.split(" ");
				switch (str.length) {
				case 6:
					addNodeByType(str[0], str[1], str[2], str[3], str[4], str[5]);
					break;
				case 7:
					addNodeByType(str[0], str[1], str[2], str[3], str[4], str[5], str[6]);
					break;
				case 8: 
					addNodeByType(str[0], str[1], str[2], str[3], str[4], str[5], str[6], str[7]);
					break;
				case 9: 
					addNodeByType(str[0], str[1], str[2], str[3], str[4], str[5], str[6], str[7], str[8]);
					break;
				case 10: 
					addNodeByType(str[0], str[1], str[2], str[3], str[4], str[5], str[6], str[7], str[8], str[9]);
					break;
				}
			}
			br.close();
			Layer.getMapViewer().repaint();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @return the number of the nodes
	 */
	public static int size() {
		return size;
	}

	/**
	 * @param type
	 */
	public static void addNodeByType(String... type) {
		switch (Integer.valueOf(type[0])) {
		case 1:
			add(new Sensor(type[3], type[4], type[5], type[6], type[7], type[8], type[9]));
			break;
		case 2:
			add(new Gas(type[3], type[4], type[5]));
			break;
		case 3:
			if(type.length==5)
				add( new SimpleInsect(type[3], type[4], type[5], type[6]));
			if(type.length==4)
				add(new SimpleInsect(type[3], "", type[5], type[6]));
			break;
		case 4:
			add(new BaseStation(type[3], type[4], type[5], type[6]));			
			break;
		case 5:
			add(new Router(type[3], type[4], type[5], type[6]));
			break;
		case 6:
			add(new Mobile(type[3], type[4], type[5], type[6]));
			break;
		case 7:
			add(new MobileWithRadio(type[3], type[4], type[5], type[6], type[7]));
			break;
		case 8:
			add(new Marker(type[3], type[4], type[5]));
			break;
		case 9:
			add(new StreetVertex(type[3], type[4], type[5], type[6]));
			break;
		}
	}

	/**
	 * @param node
	 */
	public static void add(Device node) {
		nodes.add(node);
		size++;
	}

	//public void drawDistance(int x, int y, int x2, int y2, int d, Graphics g) {
	//	g.setColor(UColor.WHITED_TRANSPARENT);
	//	g.drawString(""+d,(x2-x)/2,(y2-y)/2);
	//}
	
	/**
	 * @param g
	 */
	public void dessiner(Graphics g) {
		Device n1 = null;
		Device n2 = null;
	
		ListIterator<Device> iterator ;
		ListIterator<Device> iterator2;
		try{
		if (liens || liensDetection) {
			iterator = nodes.listIterator();
			while (iterator.hasNext() && iterator.nextIndex() < size - 1) {
				n1 = iterator.next();
				iterator2 = nodes.listIterator(iterator.nextIndex());
				while (iterator2.hasNext()) {
					n2 = iterator2.next();
					if(liensDetection) {						
						if (n1.detection(n2)) {		
							n1.setDetection(true);
						}
						if (n2.detection(n1)) {		
							n2.setDetection(true);
						}
					}					
				}
			}			
		}
		
		for (Device n : nodes) {
			n.draw(g);
			n.setDetection(false);
			if(n.getType()==Device.SENSOR) {
				((Sensor)n).drawSelectedByAlgo(g);
			}
		}		
		if (liens || liensDetection) {
			iterator = nodes.listIterator();
			while (iterator.hasNext() && iterator.nextIndex() < size - 1) {
				n1 = iterator.next();
				iterator2 = nodes.listIterator(iterator.nextIndex());
				while (iterator2.hasNext()) {
					n2 = iterator2.next();
					if (n1.radioDetect(n2) && liens) {
						n1.drawRadioLink(n2, g);
						if(displayConnectionDistance) {						
							Layer.drawDistance(n1.getX(),n1.getY(),n2.getX(),n2.getY(),(int)n1.distance(n2),g);
						}
					}
					if(liensDetection) {
						if (n1.detection(n2)) {		
							n1.drawDetectionLink(n2, g);
						}
						if (n2.detection(n1)) {		
							n2.drawDetectionLink(n1, g);
						}
					}
				}
			}			
		}
		}catch(Exception e) {}
	}
	
	/**
	 * @param g
	 */
	public void dessiner2(Graphics g) {
		//Device n1 = null;
		//Device n2 = null;
	
		//ListIterator<Device> iterator ;
		//ListIterator<Device> iterator2;		
		/*
		if (liens || liensDetection) {
			iterator = nodes.listIterator();
			while (iterator.hasNext() && iterator.nextIndex() < size - 1) {
				n1 = iterator.next();
				iterator2 = nodes.listIterator(iterator.nextIndex());
				while (iterator2.hasNext()) {
					n2 = iterator2.next();
					if(liensDetection) {						
						if (n1.detection(n2)) {		
							n1.setDetection(true);
						}
						if (n2.detection(n1)) {		
							n2.setDetection(true);
						}
					}					
				}
			}			
		}*/
		
		for (Device n : nodes) {
			n.draw(g);
			n.setDetection(false);
			if(n.getType()==Device.SENSOR) {
				((Sensor)n).drawSelectedByAlgo(g);
			}
		}
		
		g.setColor(Color.black);
		for (Point [] p : linksCoord) {
			g.drawLine(p[0].x, p[0].y, p[1].x, p[1].y);
		}
		
		
		/*
		
		if (liens || liensDetection) {
			iterator = nodes.listIterator();
			while (iterator.hasNext() && iterator.nextIndex() < size - 1) {
				n1 = iterator.next();
				iterator2 = nodes.listIterator(iterator.nextIndex());
				while (iterator2.hasNext()) {
					n2 = iterator2.next();
					if (n1.radioDetect(n2) && liens) {
						n1.drawRadioLink(n2, g);
						if(displayConnectionDistance) {						
							drawDistance(n1.getX(),n1.getY(),n2.getX(),n2.getY(),(int)n1.distance(n2),g);
						}
					}
					if(liensDetection) {
						if (n1.detection(n2)) {		
							n1.drawDetectionLink(n2, g);
						}
						if (n2.detection(n1)) {		
							n2.drawDetectionLink(n1, g);
						}
					}
				}
			}			
		}*/
	}
	
	

	// public Graph toGraph2() {
	// Node n1 = null ;
	// Node n2 = null ;
	// double distance = 0 ;
	// Graph graphe = new Graph() ;
	// for(int i=0; i<nodes.size(); i++) {
	// graphe.add(new Vertex(i,nodes.get(i).getNodeName()));
	// }
	// for (int i = 0; i < nodes.size() - 1; i++) {
	// n1 = nodes.get(i) ;
	// for (int j = i + 1; j < nodes.size(); j++) {
	// n2 = nodes.get(j) ;
	// if(n1.radioDetect(n2)) {
	// distance = n1.distance(n2);
	// graphe.get(i).ajouterVoisin(graphe.get(j), distance) ;
	// graphe.get(j).ajouterVoisin(graphe.get(i), distance) ;
	// }
	// }
	// }
	// return graphe ;
	// }

	public Device get(int idx) {
		return nodes.get(idx);
	}

	public void setLiens(boolean b) {
		liens = b;
	}
	
	public boolean getLiens() {
		return liens ;
	}
	
	public void setLiensDetection(boolean b) {
		liensDetection = b;
	}

	public boolean getLiensDetection() {
		return liensDetection ;
	}

	public void setDisplayDistance(boolean b) {
		displayConnectionDistance = b ;
	}
	
	public boolean getDisplayDistance() {
		return displayConnectionDistance ;
	}
	
	public void delete(int idx) {
		Device node = nodes.get(idx);
		Layer.getMapViewer().removeMouseListener(node);
		Layer.getMapViewer().removeMouseMotionListener(node);
		Layer.getMapViewer().removeKeyListener(node);
		nodes.remove(idx);
		size--;
		node = null;
	}

	public void simulate() {
		Device node;
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			if (node.isSelected())
				node.start();

		}
	}

	public void simulateAll() {
		Device node;
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			node.setSelection(true);
			node.start();

		}
	}

	public static StringBuilder displaySensorGraph() {
		return SensorSetCover.toSensorGraph(nodes, size).displayNames();
	}
	
	public static StringBuilder displaySensorTargetGraph() {
		return SensorSetCover.toSensorTargetGraph(nodes, size).displayNames();
	}

	public void selectInNodeSelection(int cadreX1, int cadreY1, int cadreX2,
			int cadreY2) {
		Device node;
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			node.setMove(false);
			node.setSelection(false);
			if (Layer.inMultipleSelection(node.getX(), node.getY(), cadreX1,
					cadreX2, cadreY1, cadreY2)) {
				node.setSelection(true);
			}
		}
	}

	public void deleteIfSelected() {
		Device node;
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			if (node.isSelected()) {
				Layer.getMapViewer().removeMouseListener(node);
				Layer.getMapViewer().removeMouseMotionListener(node);
				Layer.getMapViewer().removeKeyListener(node);
				iterator.remove();
				size--;
				node = null;
			}
		}
	}

	public static void setGpsFileName(String gpsFileName) {
		Device node;
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			if (node.isSelected()) {
				node.setGPSFileName(gpsFileName);
				Layer.getMapViewer().repaint();
			}
		}
	}
	
	public static void setComFileName(String comFileName) {
		Device node;
		System.out.println("Appel ..."+ comFileName);
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			if (node.isSelected()) {
				node.setCOMFileName(comFileName);
			}
		}
	}
	
	public static void updateFromMap(String xS, String yS, String radiusS,
			String radioRadiusS, String captureRadiusS, String gpsFileName) {
		Device node;
		for (Iterator<Device> iterator = nodes.iterator(); iterator.hasNext();) {
			node = iterator.next();
			if (node.isSelected()) {
				node.setX(Double.valueOf(xS));
				node.setY(Double.valueOf(yS));
				node.setRadius(Double.valueOf(radiusS));
				node.setRadioRadius(Double.valueOf(radioRadiusS));
				node.setCaptureRadius(Double.valueOf(captureRadiusS));
				node.setGPSFileName(gpsFileName);
				Layer.getMapViewer().repaint();
			}
		}
	}
	
	//public static void setCover1() {
	//	Graph gr = toSensorGraph() ;
	//	gr.display();
	//}

	//public Graph getSensorGraph() {
	//	return toSensorGraph() ;
	//}
	
	

	public static void initActivation() {
		for (Device device : nodes) {
			device.setAlgoSelect(false);
		}
		Layer.getMapViewer().repaint();
	}
	
	public static void initSelectedActivation() {
		for (Device device : nodes) {
			if(device.isSelected())
				device.setAlgoSelect(false);
		}
		Layer.getMapViewer().repaint();
	}
	
	public static void setAlgoSelect(boolean b) {
		for(Device node : nodes) {
			node.setAlgoSelect(false);
		}
		Layer.getMapViewer().repaint();
	}

	public void setSelectionOfAllNodes(boolean selection, int type, boolean addSelect) {
		for (Device dev : nodes) {
			if(!addSelect) dev.setSelection(false);
			if(dev.getType() == type || type==-1) 
				dev.setSelection(selection);
		}
		Layer.getMapViewer().repaint();
	}

	public void invertSelection() {
		for (Device dev : nodes) { 
			dev.invSelection();
		}
		Layer.getMapViewer().repaint();
	}
	
	//@Override
	public void run() {
		LinkedList<Point []> tLinksCoord ;
		while(true) {
			tLinksCoord = new LinkedList<Point []>();
			//linksCoord = new LinkedList<Point []>();
			Device n1 = null;
			Device n2 = null;
		
			//ListIterator<Device> iterator ;
			//ListIterator<Device> iterator2;		
			
			if (liens || liensDetection) {
				//iterator = nodes.listIterator();
				//while (iterator.hasNext() && iterator.nextIndex() < size - 1) {
				for(int i=0; i<nodes.size()-1; i++) {
					n1 = nodes.get(i);
					//n1 = iterator.next();
					//iterator2 = nodes.listIterator(iterator.nextIndex());
					//while (iterator2.hasNext()) {
					for(int j=i+1; j<nodes.size(); j++) {
						n2 = nodes.get(j);
						//n2 = iterator2.next();
						if (n1.radioDetect(n2) && liens) {
							
							tLinksCoord.add(getCouple(n1,n2));
							//if(displayConnectionDistance) {						
							//drawDistance(n1.getX(),n1.getY(),n2.getX(),n2.getY(),(int)n1.distance(n2),g);
							//}
						}
					}
				}
			}
			try {
				linksCoord = tLinksCoord;
				Layer.getMapViewer().repaint();
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Point [] getCouple(Device n1, Device n2) {
		int lx1 = MapCalc.geoToIntPixelMapX(n1.getX(),n1.getY()) ;
		int ly1 = MapCalc.geoToIntPixelMapY(n1.getX(),n1.getY()) ;
		int lx2 = MapCalc.geoToIntPixelMapX(n2.getX(),n2.getY()) ;
		int ly2 = MapCalc.geoToIntPixelMapY(n2.getX(),n2.getY()) ;
		Point [] p = new Point[2];
		p[0] = new Point(lx1,ly1);
		p[1] = new Point(lx2,ly2);
		return p ;
	}
}
