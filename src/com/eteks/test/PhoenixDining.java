package com.eteks.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JOptionPane;

import com.eteks.sweethome3d.model.CatalogPieceOfFurniture;
import com.eteks.sweethome3d.model.FurnitureCategory;
import com.eteks.sweethome3d.model.Home;
import com.eteks.sweethome3d.model.HomePieceOfFurniture;
import com.eteks.sweethome3d.model.Room;
import com.eteks.sweethome3d.model.Wall;
import com.eteks.sweethome3d.plugin.Plugin;
import com.eteks.sweethome3d.plugin.PluginAction;


public class PhoenixDining extends Plugin 
{

	public List<String> furnIds = new ArrayList<String>();
	public List<float[][]> furnRects = new ArrayList<float[][]>();
	public List<Float> furnThicks = new ArrayList<Float>();
	public List<float[][]> furnRectsAccess = new ArrayList<float[][]>();

	public List<String> markBoxName = new ArrayList<String>();

	public class RoomTestAction extends PluginAction 
	{		
		public Home home = null;
		public Room room = null;
		public Room foyer = null;
		public Room diningRoom = null;

		public HomePieceOfFurniture diningRect = null;		

		public int MARKBOX_COUNT = 6;
		public HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[MARKBOX_COUNT];
		
		public float ROOM_TOLERANCE = 0.51f;
		public float FURN_TOLERANCE = 0.51f;
		
		public float ORIENTATION_TOLERANCE = 0.05f;
		public float FURNITURE_PLACE_TOLERANCE = 0.0f;	// 0cm  // 5cm
		
		public float TOLERANCE_PERCENT = 0.02f;			//0.05f;		
		public float INFINITY_COORDS = 10000.0f;
		
		public float FURNITURE_BLOAT_SIZE = 2.0f;		// 2cm
		
		public float DINING_PLACE_STEP = 30.0f;			// 1 ft
		
		public float tolerance = 0.5f; 					// 5 mm
		
		public boolean bShowMarker = true;
		
		public Points initPoints = null;
		
		public List<Design> validDesignList = new ArrayList<Design>();
		
		public String DiningRectStr = "";
		public String DiningRectPlaceStr = "";
		
		// ======================= CLASSES ======================= //

		public class Points
		{
			float x;
			float y;

			public Points()
			{
				x = -10.0f;
				y = -10.0f;
			}
			
			public Points(float xCoord , float yCoord)
			{
				x = xCoord;
				y = yCoord;
			}
		}				

		public class LineSegement
		{
			Points startP;		// x, y
			Points endP;		// x, y
			Points parent;		// x, y
			int[] parentIndex;

			public LineSegement(Points sP, Points eP)
			{
				startP = sP;
				endP = eP;
			}
		}

		public class FurnLoc
		{
			float w;
			float h;
			float el;
			float ang;
			Points p;
			
			public FurnLoc(float wIn, float hIn, float elIn, float angIn, Points coord)
			{
				w = wIn;
				h = hIn;
				el = elIn;
				ang = angIn;
				p = coord;
			}
			
			public FurnLoc()
			{
				w = 0.0f;
				h = 0.0f;
				el = 0.0f;
				ang = 0.0f;
			}
		}
		
		public class Intersect
		{
			Points p;
			float dist;
			
			public Intersect(Points inP, float inD)
			{
				p = inP;
				dist = inD;
			}
		}
		
		public class ChordPoints
		{
			Points p;
			int lsIndx;
			
			public ChordPoints(Points inP, int inLSIndx)
			{
				p = inP;
				lsIndx = inLSIndx;
			}
		}
		
		public class ChordSegements
		{
			Points startP;
			Points endP;
			int startLSIndx;
			int endLSIndx;
			
			public ChordSegements(Points sP, Points eP, int sIndx, int eIndx)
			{
				startP = sP;
				endP = eP;
				startLSIndx = sIndx;
				endLSIndx = eIndx;
			}
		}
		
		public class Accessibility
		{
			boolean bAddAccess;
			float accessWidth;
			float accessDepth;
			
			public Accessibility(boolean bAccess, float accW, float accD)
			{
				bAddAccess = bAccess;
				accessWidth = accW;
				accessDepth = accD;
			}
		}
		
		public class Design
		{
			Points p;
			float orient;

			public Design(Points inP , float inOrient)
			{
				p = inP;
				orient = inOrient;
			}
		}
		
		
		// ======================= CODE ======================= //
		
		public RoomTestAction() 
		{
			putPropertyValue(Property.NAME, "PhoenixDining");
			putPropertyValue(Property.MENU, "Phoenix-Fresh");

			// Enables the action by default
			setEnabled(true);
		}	

		@Override
		public void execute() 
		{	
			try
			{
				home = getHome();
				room = home.getRooms().get(0);
				markBoxes = getMarkerBoxes();	
				
				storeAllFurnRects(home);
				storeAllWallRects(home);
				
				getDiningRoom();
				
				
				long startTime = System.currentTimeMillis(); //System.nanoTime();
				
				// ===================================================== //	
				
				// 1. -------------------------------- //
				/*
				float accessWidth = 61.0f;	// 2ft.
				float accessDepth = 61.0f;	// 2ft.
				
				for(HomePieceOfFurniture hp : home.getFurniture())
				{
					if(hp.getName().equalsIgnoreCase("diningrect"))
					{
						putMarkerOnPolygon(hp.getPoints(), 2);
						
						float[][] fRect = genAccessBox(hp, accessWidth, accessDepth);
						putMarkerOnPolygon(fRect, 1);
					}
				}
				*/
				// 3. -------------------------------- //
				/*
				Points centerP = getStartingPoint();
				putMarkers(centerP, 0);
				
				float radius = 300.0f; // 282.0f; //9.25 ft
				getDiningRoom();
				
				float[][] polyRect = diningRoom.getPoints();
				putMarkerOnPolygon(polyRect, 4);
				
				getIntersectionCirclePoly(centerP, radius, polyRect, tolerance);
				
				*/
				// 4. -------------------------------- //
				/*
				Points centerP = getStartingPoint();
				putMarkers(centerP, 0);
				
				float radius = 300.0f; 
				
				calcStartArcPoints(centerP, radius, tolerance);
				
				Points pArc1 = new Points(128.0f, 1127.0f);
				Points pArc2 = new Points(499.0f, 983.0f);
				putMarkers(pArc1, 3);
				putMarkers(pArc2, 4);
				
				generateFreeArcSegs(centerP, pArc1, pArc2, radius, tolerance);
				*/
				
				// A. Intersection with All furns -------------------------------- //
				/*
				HomePieceOfFurniture hpf = getFurnItem("diningrectchairs");

				boolean bAddAccessibility = true;
				float accessWidth = 61.0f;	// 2ft.
				float accessDepth = 61.0f;	// 2ft.
						
				Accessibility accessBox = new Accessibility(bAddAccessibility, accessWidth, accessDepth);
				HomePieceOfFurniture hpfNew = hpf.clone();				
				Points furnCoords = new Points(250.0f, 700.0f);
				
				LineSegement ws = getLongestSideOfRoom(diningRoom);
				placeFurnParallelToWall(ws, hpfNew, furnCoords);
				//placeFurnPerpendicularToWall(ws, hpfNew, furnCoords);
				
				checkPlacement(hpfNew, accessBox);
				*/
				
				// B. Check initial placements -------------------------------- //
				// C. Longest free arc segment -------------------------------- //
				// D. Calculation of 4 move points (new)----------------------- //
				// E. Move and incremental placement  ----------------------- //
				// F. Calculation of 4 move points for ALL increments ----------------------- //
				// G. Calculation of 4 move points for ALL increments with increasing radius ----------------------- //
				
				boolean bAddAccessibility = true;
				float accessWidth = 61.0f;	// 2ft.
				float accessDepth = 61.0f;	// 2ft.
				
				float DINING_MOVE_STEP = 30.5f;					
				int DINING_MOVE_MAX = 3;
				
				//float DINING_RADIUS_START = 259.0f;	// 8.5 ft
				//float DINING_RADIUS_END = 304.8f;	// 10 ft
				
				float DINING_RADIUS_START = 274.3f;	// 9 ft
				float DINING_RADIUS_END = 335.3f;	// 11 ft
				
				float DINING_INCREMENT = (DINING_RADIUS_END - DINING_RADIUS_START)/(6.0f*2.54f);

				DiningRectStr = "diningrect";
				DiningRectPlaceStr = "diningrectchairs";
				
				for(int d = 0 ; d < DINING_INCREMENT; d++)
				{
					float DINING_RADIUS = DINING_RADIUS_START + (d*DINING_INCREMENT);
							
					Accessibility accessBox = new Accessibility(bAddAccessibility, accessWidth, accessDepth);
					
					HomePieceOfFurniture newFurn = getFurnItem(DiningRectStr);
					HomePieceOfFurniture newFurnPlace = getFurnItem(DiningRectPlaceStr);
					
					Points centerP = getStartingPoint();
					List<Points> initP = calcInitialPoints(centerP, DINING_RADIUS, tolerance);
					
					boolean bSuccess = false;
							
					if(initP.size() > 0)
					{
						initPoints = initP.get(0);
						
						if(!checkPointBlockedInRoom(initPoints, diningRoom))
						{
							putMarkers(initPoints, 5);					
							
							HomePieceOfFurniture newFurn0 = newFurn.clone();
							newFurn0.setName(newFurn.getName() + "_0");
							
							HomePieceOfFurniture newFurnPlace0 = newFurnPlace.clone();
							newFurnPlace0.setName(newFurnPlace.getName() + "_0");
							
							LineSegement longWS = getLongestSideOfRoom(diningRoom);										
		
							bSuccess = checkInitialPlacements(longWS, newFurn0, newFurnPlace0, initPoints, accessBox);
							
							// E. ---------------------------------- //
							
							float loopCount = 0;
							float moveCount = 0;
							
							while(!bSuccess)
							{
								loopCount++;
								moveCount = (loopCount * DINING_MOVE_STEP);
								
								List<Points> moveP = calcAllFourFurnCoordinates(longWS, initPoints, moveCount, tolerance);
								
								for(int m = 0; m < moveP.size(); m++)
								{
									Points p = moveP.get(m);
									
									if(!checkPointBlockedInRoom(p, diningRoom))
									{
										putMarkers(p, 4);
										
										HomePieceOfFurniture newFurnM = newFurn.clone();
										newFurnM.setName(newFurn.getName() + "_" + m + "_" + loopCount);
									
										HomePieceOfFurniture newFurnPlaceM = newFurnPlace.clone();
										newFurnPlaceM.setName(newFurnPlace.getName() + "_" + m);
										
										bSuccess = checkNextPlacements(longWS, newFurnM, newFurnPlaceM, p, accessBox);	
									}
								}
								
								if(loopCount >= DINING_MOVE_MAX)
									break;
							}
						}
					}
					
					// ================================================ //
				}
				
				long endTime = System.currentTimeMillis(); //System.nanoTime();
				
				JOptionPane.showMessageDialog(null, "Time : " + (endTime - startTime) + " ms \n ");
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				//e.printStackTrace();
			}
		}		
		
		public List<Points> getIntersectionCirclePoly(Points center, float rad, float[][] polyRect, float tolr)
		{
			List<Points> finalPList = new ArrayList<Points>(); 
			
			if(polyRect.length == 2)
			{
				Points startP = new Points(polyRect[0][0], polyRect[0][1]);
				Points endP = new Points(polyRect[1][0], polyRect[1][1]);
				
				List<Points> pList = getIntersectionCircleLine(center, rad, startP, endP);
				
				for(Points p : pList)
				{	
					if(checkPointInBetween(p, startP, endP, tolr))
						finalPList.add(p);
				}
			}
			
			else
			{
				for(int x = 0 ; x < polyRect.length; x++)
				{
					Points startP = new Points(polyRect[x][0], polyRect[x][1]);
					Points endP = null;
					
					if(x == (polyRect.length - 1))
						endP = new Points(polyRect[0][0], polyRect[0][1]);
					else
						endP = new Points(polyRect[x+1][0], polyRect[x+1][1]);
					
					List<Points> pList = getIntersectionCircleLine(center, rad, startP , endP);			
					
					//JOptionPane.showMessageDialog(null, pList.size());
					
					for(Points p : pList)
					{	
						if(checkPointInBetween(p, startP, endP, tolr))
							finalPList.add(p);
					}
				}
			}
			
			/*
			if(bShowMarker)
			{
				for(Points p : finalPList)
				{
					putMarkers(p, 3);
				}
			}
			*/
			
			return finalPList;
		}
		
		public boolean checkPlacement(HomePieceOfFurniture hpfN, Accessibility accessBox)
		{	
			boolean bSuccess = false;
			
			furnIds.add(hpfN.getName());
			furnRects.add(hpfN.getPoints());
			furnThicks.add(0.0f);
			
			float[][] hpfNewRect = new float[0][0];
			
			if(accessBox.bAddAccess)
			{
				hpfNewRect = genAccessBox(hpfN, accessBox.accessWidth, accessBox.accessDepth);
			}
			else
				hpfNewRect = hpfN.getPoints();			

			furnRectsAccess.add(hpfNewRect);
			//putMarkerOnPolygon(hpfNewRect, 3);
			
			boolean bIntersects = checkIntersectWithAllFurns(hpfN, accessBox.bAddAccess);			
			boolean bIsInsideRoom = checkInsideRoom(room, hpfNewRect);
		
			bSuccess = (!bIntersects) && bIsInsideRoom;
			//JOptionPane.showMessageDialog(null, "intersects : " + bIntersects + ", inRoom : " + bIsInsideRoom + " -> " + bSuccess);
			
			int indx = furnIds.indexOf(hpfN.getName());
			furnIds.remove(indx);
			furnRects.remove(indx);
			furnThicks.remove(indx);
			furnRectsAccess.remove(indx);
			
			return bSuccess;
		}
		
		public boolean checkInitialPlacements(LineSegement ws, HomePieceOfFurniture hpf, HomePieceOfFurniture hpfPlace, Points p, Accessibility accessBox)
		{
			boolean bSuccess = false;
			
			boolean bSuccessPara = false;
			boolean bSuccessPerp = false;
			
			HomePieceOfFurniture hpf0 = hpf.clone();			
			String hpf0Name = hpf0.getName() + "_para";
			hpf0.setName(hpf0Name);
			
			placeFurnParallelToWall(ws, hpf0, initPoints);		// initial placement
			
			bSuccessPara = checkPlacement(hpf0, accessBox);
			//JOptionPane.showMessageDialog(null, "para0 : " + bSuccessPara);
			
			if(bSuccessPara)
			{
				home.deletePieceOfFurniture(hpf0);
				
				HomePieceOfFurniture hpfPl0 = hpfPlace.clone();
				hpfPl0.setName(hpfPlace.getName() + "_paraP");
				placeFurnParallelToWall(ws, hpfPl0, initPoints);
				
				Design des1 = new Design(initPoints, 0.0f);
				validDesignList.add(des1);
				
				HomePieceOfFurniture hpf1 = hpf.clone();
				String hpf1Name = hpf1.getName() + "_perp";
				hpf1.setName(hpf1Name);
				
				placeFurnPerpendicularToWall(ws, hpf1, p);
				bSuccessPerp = checkPlacement(hpf1, accessBox);
				
				//JOptionPane.showMessageDialog(null, "perp0 : " + bSuccessPerp);
						
				if(bSuccessPerp)
				{	
					home.deletePieceOfFurniture(hpf1);
					
					HomePieceOfFurniture hpfPl1 = hpfPlace.clone();
					hpfPl1.setName(hpfPlace.getName() + "_perpP");
					placeFurnPerpendicularToWall(ws, hpfPl1, initPoints);
					
					Design des2 = new Design(initPoints, 0.0f);
					validDesignList.add(des2);
					
					bSuccess = true;
				}
				else
				{
					home.deletePieceOfFurniture(hpf1);
					//JOptionPane.showMessageDialog(null, " Not enough space !!!");
					
					bSuccess = false;
				}
			}
			else
			{
				home.deletePieceOfFurniture(hpf0);
				//JOptionPane.showMessageDialog(null, " Not enough space !!!");
				
				HomePieceOfFurniture hpf2 = hpf.clone();
				String hpf2Name = hpf2.getName() + "_perp";
				hpf2.setName(hpf2Name);
				
				placeFurnPerpendicularToWall(ws, hpf2, initPoints);
				bSuccessPerp = checkPlacement(hpf2, accessBox);
				
				//JOptionPane.showMessageDialog(null, "perp1 : " + bSuccessPerp);
				
				if(bSuccessPerp)
				{
					home.deletePieceOfFurniture(hpf2);
					
					HomePieceOfFurniture hpfPl2 = hpfPlace.clone();
					hpfPl2.setName(hpfPlace.getName() + "_perpP");
					placeFurnPerpendicularToWall(ws, hpfPl2, initPoints);
					
					Design des1 = new Design(p, 0.0f);
					validDesignList.add(des1);
					
					bSuccess = true; 
				}
				else
				{
					home.deletePieceOfFurniture(hpf2);
					//JOptionPane.showMessageDialog(null, " Not enough space !!!");
					
					bSuccess = false;
				}
			}
			
			return bSuccess;
		}
		
		public boolean checkNextPlacements(LineSegement ws, HomePieceOfFurniture hpf, HomePieceOfFurniture hpfPlace, Points p, Accessibility accessBox)
		{
			boolean bSuccess = false;
			
			boolean bSuccessPara = false;
			boolean bSuccessPerp = false;
			
			HomePieceOfFurniture hpf0 = hpf.clone();			
			String hpf0Name = hpf0.getName() + "_para";
			hpf0.setName(hpf0Name);
			
			placeFurnParallelToWall(ws, hpf0, p);		// initial placement
			
			bSuccessPara = checkPlacement(hpf0, accessBox);
			//JOptionPane.showMessageDialog(null, "para0 : " + bSuccessPara);
			
			if(bSuccessPara)
			{
				home.deletePieceOfFurniture(hpf0);
				
				HomePieceOfFurniture hpfPl0 = hpfPlace.clone();
				hpfPl0.setName(hpfPlace.getName() + "_paraP");
				placeFurnParallelToWall(ws, hpfPl0, p);
				
				Design des1 = new Design(p, 0.0f);
				validDesignList.add(des1);
				
				HomePieceOfFurniture hpf1 = hpf.clone();
				String hpf1Name = hpf1.getName() + "_perp";
				hpf1.setName(hpf1Name);
				
				placeFurnPerpendicularToWall(ws, hpf1, p);
				bSuccessPerp = checkPlacement(hpf1, accessBox);
				
				//JOptionPane.showMessageDialog(null, "perp0 : " + bSuccessPerp);
						
				if(bSuccessPerp)
				{	
					home.deletePieceOfFurniture(hpf1);
					
					HomePieceOfFurniture hpfPl1 = hpfPlace.clone();
					hpfPl1.setName(hpfPlace.getName() + "_perpP");
					placeFurnPerpendicularToWall(ws, hpfPl1, p);
					
					Design des2 = new Design(p, 0.0f);
					validDesignList.add(des2);
					
					bSuccess = true;
				}
				else
				{
					home.deletePieceOfFurniture(hpf1);
					//JOptionPane.showMessageDialog(null, " Not enough space !!!");
					
					bSuccess = false;
				}
			}
			else
			{
				home.deletePieceOfFurniture(hpf0);
				//JOptionPane.showMessageDialog(null, " Not enough space !!!");
				
				HomePieceOfFurniture hpf2 = hpf.clone();
				String hpf2Name = hpf2.getName() + "_perp";
				hpf2.setName(hpf2Name);
				
				placeFurnPerpendicularToWall(ws, hpf2, p);
				bSuccessPerp = checkPlacement(hpf2, accessBox);
				
				//JOptionPane.showMessageDialog(null, "perp1 : " + bSuccessPerp);
				
				if(bSuccessPerp)
				{
					home.deletePieceOfFurniture(hpf2);
					
					HomePieceOfFurniture hpfPl2 = hpfPlace.clone();
					hpfPl2.setName(hpfPlace.getName() + "_perpP");
					placeFurnPerpendicularToWall(ws, hpfPl2, p);
					
					Design des1 = new Design(p, 0.0f);
					validDesignList.add(des1);
					
					bSuccess = true; 
				}
				else
				{
					home.deletePieceOfFurniture(hpf2);
					//JOptionPane.showMessageDialog(null, " Not enough space !!!");
					
					bSuccess = false;
				}
			}
			
			return bSuccess;
		}
		
		public List<Points> calcInitialPoints(Points center, float rad, float tolr)
		{
			List<Points> arcPoints = new ArrayList<Points>();
			List<LineSegement> validArcs = calcStartArcPoints(center, rad, tolr);
			 
			LineSegement freeWS = calcLongestFreeArcSeg(validArcs, center, rad, tolr);
			
			if(freeWS != null)
			{
				//putMarkers(freeWS.startP, 3);
				//putMarkers(freeWS.endP, 0);
				
				float slope = ((freeWS.endP.y - freeWS.startP.y) / (freeWS.endP.x - freeWS.startP.x));
				float slopePerp = (-1 / slope);
				
				float intercept = center.y - (slopePerp *center.x);			
				List<Points>  interPList = getIntersectionCircleLine2(center, rad, slopePerp, intercept);
				
				for(Points p : interPList)
				{
					if(diningRoom.containsPoint(p.x, p.y, ROOM_TOLERANCE))
						arcPoints.add(p);
				}
				
				//JOptionPane.showMessageDialog(null, "arcPoints : " + arcPoints.size());
			}
			
			return arcPoints;
		}
		
		// -x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x-x //
				
		public List<Points> calcAllFourFurnCoordinates(LineSegement ws, Points centerP, float dist, float tolr) 
		{
			List<Points> retPList = new ArrayList<Points>();
			
			Points wsMidP = new Points(((ws.startP.x + ws.endP.x)/2),(ws.startP.y + ws.endP.y)/2);
			
			//putMarkers(ws.startP, 1);
			//putMarkers(ws.endP, 1);
			
			float xLimit = Math.abs(ws.endP.x - ws.startP.x);
			float yLimit = Math.abs(ws.endP.y - ws.startP.y);
			
			//JOptionPane.showMessageDialog(null, "xLimit:" + xLimit + ", yLimit:" + yLimit + ", tolr:" + tolr);
					 
			if(yLimit < tolr)
			{
				// Perpendicular - towards longest wall
				if(yLimit < tolr)
				{
					Points p1 = new Points(centerP.x, (centerP.y + dist));
					Points p2 = new Points(centerP.x, (centerP.y - dist));
					
					//JOptionPane.showMessageDialog(null, "1_ p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
					
					List<Points> interPList2 = new ArrayList<Points>();
					interPList2.add(p1);
					interPList2.add(p2);
					
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					retPList.addAll(sortedPList2);
				}
				else if(yLimit >= tolr)
				{
					float slopePara = ((ws.endP.y - ws.startP.y) / (ws.endP.x - ws.startP.x));
					float slopePerp = (-1.0f / slopePara);
					float intercept = centerP.y - (slopePerp * centerP.x);
					
					//JOptionPane.showMessageDialog(null, "1_ slopePara : " + slopePara + ",\nslopePerp : " + slopePerp);
					
					List<Points> interPList2 = getIntersectionCircleLine2(centerP, dist, slopePerp, intercept);
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					
					retPList.addAll(sortedPList2);
				}
				
				//JOptionPane.showMessageDialog(null, slopePerp + "/ interceptPerp : " + intercept);
				
				// Parallel - to longest wall
				if(xLimit < tolr)
				{
					Points p1 = new Points((centerP.x + dist), centerP.y);
					Points p2 = new Points((centerP.x - dist), centerP.y);
					
					//JOptionPane.showMessageDialog(null, "1|| p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
							
					List<Points> interPList1 = new ArrayList<Points>();
					interPList1.add(p1);
					interPList1.add(p2);
					
					List<Points> sortedPList1 = sortPList(interPList1, wsMidP);
					retPList.addAll(sortedPList1);
				}
				else if(xLimit >= tolr)
				{
					float slopePara = ((ws.endP.y - ws.startP.y) / (ws.endP.x - ws.startP.x));
					float intercept = centerP.y - (slopePara * centerP.x);
					
					//JOptionPane.showMessageDialog(null, "1|| slopePara : " + slopePara + ",\n");
					
					List<Points> interPList1 = getIntersectionCircleLine2(centerP, dist, slopePara, intercept);				
					List<Points> sortedPList1 = sortPList(interPList1, wsMidP);
					
					retPList.addAll(sortedPList1);
				}			
				//JOptionPane.showMessageDialog(null, slopePara + "/ interceptPara : " + intercept);
			}
			else if(xLimit < tolr)
			{
				// Perpendicular - towards longest wall
				if(xLimit < tolr)
				{
					Points p1 = new Points((centerP.x + dist), centerP.y);
					Points p2 = new Points((centerP.x - dist), centerP.y);
					
					//JOptionPane.showMessageDialog(null, "2_ p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
							
					List<Points> interPList1 = new ArrayList<Points>();
					interPList1.add(p1);
					interPList1.add(p2);
					
					List<Points> sortedPList1 = sortPList(interPList1, wsMidP);
					retPList.addAll(sortedPList1);
				}
				else if(xLimit >= tolr)
				{
					float slopePara = ((ws.endP.y - ws.startP.y) / (ws.endP.x - ws.startP.x));
					float slopePerp = (-1.0f / slopePara);
					float intercept = centerP.y - (slopePerp * centerP.x);
					
					//JOptionPane.showMessageDialog(null, "2_ slopePara : " + slopePara + ",\nslopePerp : " + slopePerp);
					
					List<Points> interPList1 = getIntersectionCircleLine2(centerP, dist, slopePerp, intercept);				
					List<Points> sortedPList1 = sortPList(interPList1, wsMidP);
					
					retPList.addAll(sortedPList1);
				}			
				//JOptionPane.showMessageDialog(null, slopePerp + "/ interceptPerp : " + intercept);
				
				// Parallel - to longest wall
				if(yLimit < tolr)
				{
					Points p1 = new Points(centerP.x, (centerP.y + dist));
					Points p2 = new Points(centerP.x, (centerP.y - dist));
					
					//JOptionPane.showMessageDialog(null, "2|| p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
					
					List<Points> interPList2 = new ArrayList<Points>();
					interPList2.add(p1);
					interPList2.add(p2);
					
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					retPList.addAll(sortedPList2);
				}
				else if((yLimit >= tolr) && (xLimit < tolr))
				{					
					Points p1 = new Points(centerP.x, (centerP.y + dist));
					Points p2 = new Points(centerP.x, (centerP.y - dist));
					
					//JOptionPane.showMessageDialog(null, "2|| p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
					
					List<Points> interPList2 = new ArrayList<Points>();
					interPList2.add(p1);
					interPList2.add(p2);
					
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					retPList.addAll(sortedPList2);
				}	
				else if(yLimit >= tolr)
				{					
					float slopePara = ((ws.endP.y - ws.startP.y) / (ws.endP.x - ws.startP.x));
					float intercept = centerP.y - (slopePara * centerP.x);
					
					//JOptionPane.showMessageDialog(null, "2|| slopePara : " + slopePara + ",\n");
					
					List<Points> interPList2 = getIntersectionCircleLine2(centerP, dist, slopePara, intercept);
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					
					retPList.addAll(sortedPList2);
				}
				//JOptionPane.showMessageDialog(null, slopePara + "/ interceptPara : " + intercept);
				
			}
			else
			{
				// Perpendicular - towards longest wall
				if(yLimit < tolr)
				{
					Points p1 = new Points(centerP.x, (centerP.y + dist));
					Points p2 = new Points(centerP.x, (centerP.y - dist));
					
					//JOptionPane.showMessageDialog(null, "3_ p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
					
					List<Points> interPList2 = new ArrayList<Points>();
					interPList2.add(p1);
					interPList2.add(p2);
					
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					retPList.addAll(sortedPList2);
				}
				else if(yLimit >= tolr)
				{
					float slopePara = ((ws.endP.y - ws.startP.y) / (ws.endP.x - ws.startP.x));
					float slopePerp = (-1.0f / slopePara);
					float intercept = centerP.y - (slopePerp * centerP.x);
					
					//JOptionPane.showMessageDialog(null, "3_ slopePara : " + slopePara + ",\nslopePerp : " + slopePerp);
					
					List<Points> interPList2 = getIntersectionCircleLine2(centerP, dist, slopePerp, intercept);
					List<Points> sortedPList2 = sortPList(interPList2, wsMidP);
					
					retPList.addAll(sortedPList2);
				}			
				//JOptionPane.showMessageDialog(null, slopePerp + "/ interceptPerp : " + intercept);
				
				// Parallel - to longest wall
				if(xLimit < tolr)
				{
					Points p1 = new Points((centerP.x + dist), centerP.y);
					Points p2 = new Points((centerP.x - dist), centerP.y);
					
					//JOptionPane.showMessageDialog(null, "3|| p1 : " + p1.x + ", " + p1.y + ",\np2 : " + p2.x + ", " + p2.y);
							
					List<Points> interPList1 = new ArrayList<Points>();
					interPList1.add(p1);
					interPList1.add(p2);
					
					List<Points> sortedPList1 = sortPList(interPList1, wsMidP);
					retPList.addAll(sortedPList1);
				}
				else if(xLimit >= tolr)
				{
					float slopePara = ((ws.endP.y - ws.startP.y) / (ws.endP.x - ws.startP.x));
					float intercept = centerP.y - (slopePara * centerP.x);
					
					//JOptionPane.showMessageDialog(null, "3|| slopePara : " + slopePara + ",\n");
					
					List<Points> interPList1 = getIntersectionCircleLine2(centerP, dist, slopePara, intercept);				
					List<Points> sortedPList1 = sortPList(interPList1, wsMidP);
					
					retPList.addAll(sortedPList1);
				}			
				//JOptionPane.showMessageDialog(null, slopePara + "/ interceptPara : " + intercept);
			}
			
			/*if(bShowMarker)
			{
				for(Points p : retPList)
				{
					putMarkers(p, 1);
				}
			}*/
			
			//JOptionPane.showMessageDialog(null, retPList.size());
			return retPList;
		}
		
		public void placeFurnParallelToWall(LineSegement ws, HomePieceOfFurniture furn, Points furnCoords)
		{
			FurnLoc furnLoc = new FurnLoc();
			float furnAngle =  calcWallAngles(ws);
			
			furnLoc.w = furn.getWidth();
			furnLoc.ang = furnAngle;			
			furnLoc.p = furnCoords;	
			
			placeFurnItem(furn, furnLoc);
		}
		
		public void placeFurnPerpendicularToWall(LineSegement ws, HomePieceOfFurniture furn, Points furnCoords)
		{
			FurnLoc furnLoc = new FurnLoc();
			float furnAngle = calcWallAngles(ws);
			
			furnAngle += (float)(Math.PI/2.0f);
			
			furnLoc.w = furn.getWidth();
			furnLoc.ang = furnAngle;			
			furnLoc.p = furnCoords;	
			
			placeFurnItem(furn, furnLoc);
		}
		
		public float calcWallAngles(LineSegement ws)
		{
			float retAngle = 0.0f;
			
			float wsAngle =  (float) Math.atan((Math.abs(ws.endP.y - ws.startP.y)) / (Math.abs(ws.endP.x - ws.startP.x))); 
			
			Points p = new Points((ws.startP.x - ws.endP.x), (ws.startP.y - ws.endP.y));
			int qIndx = getQuadrantInfo(p);
			
			if(qIndx == 1)
				retAngle = wsAngle;
			else if(qIndx == 2)
				retAngle = (float)(Math.PI) - wsAngle;
			else if(qIndx == 3)
				retAngle = (float)(Math.PI) + wsAngle;
			else if(qIndx == 4)
				retAngle = (float)(2.0f*Math.PI) - wsAngle;
			
			return retAngle;
		}
		
 		public LineSegement getLongestSideOfRoom(Room r)
		{
			LineSegement longSeg = null;		

			float maxLen = 0.0f;
			float[][] roomRect = r.getPoints();
			
			if(roomRect.length > 2)
			{			
				for(int f = 0; f < roomRect.length; f++)
				{
					Points startLine = new Points(roomRect[f][0], roomRect[f][1]);					
					Points endLine = null;
					
					if(f == (roomRect.length - 1))
						endLine = new Points(roomRect[0][0], roomRect[0][1]);
					else
						endLine = new Points(roomRect[f+1][0], roomRect[f+1][1]);				
					
					float len = calcDistance(startLine, endLine);
					
					if(len > maxLen)
					{
						maxLen = len;
						longSeg = new LineSegement(startLine, endLine);
					}
				}
			}
			
			return longSeg;
		}
		
 		public LineSegement calcLongestFreeArcSeg(List<LineSegement> lsList ,Points center, float rad, float tolerance)
 		{
 			LineSegement maxLS = null;
 			float maxLength = 0.0f;
 			
			//JOptionPane.showMessageDialog(null, "lsList : " + lsList.size());
				
 			for(LineSegement ls : lsList)
 			{ 				
 				LineSegement longWS = getLongestFreeArcSeg(center, ls.startP, ls.endP, rad, tolerance);
 				
 				if(longWS != null)
 				{
 					float dist = calcDistance(longWS.startP, longWS.endP);
 				
					if(dist > maxLength)
					{
						maxLength = dist;
						maxLS = longWS;
					}
					
					//JOptionPane.showMessageDialog(null, "-->>>" + maxLength + "/ " + longWS.startP + ", " + longWS.endP);
				}
 			}
 			
 			//putMarkers(maxLS.startP, 1);
 			//putMarkers(maxLS.endP, 2);
 			
 			return maxLS;
 		}
 		
		public LineSegement getLongestFreeArcSeg(Points center, Points pArc1, Points pArc2, float rad, float tolerance)
		{			
			LineSegement maxLS = null;
			
			List<LineSegement> arcSegList = generateFreeArcSegs(center, pArc1, pArc2, rad, tolerance);
			float maxLength = 0.0f;
			
			for(LineSegement ls : arcSegList)
			{
				float dist = calcDistance(ls.startP, ls.endP);
				
				if(dist > maxLength)
				{
					maxLength = dist;
					maxLS = ls;
				}
			}
			
			return maxLS;			
		}
		
		public List<LineSegement> generateFreeArcSegs(Points center, Points pArc1, Points pArc2, float rad, float tolerance)
		{
			List<LineSegement> arcSegList = new ArrayList<LineSegement>();
			
			List<Points> interPList = getIntersectionInHome(center, pArc1, pArc2, rad, tolerance);
			
			/*
			if(bShowMarker)
			{	
				for(Points p : interPList)
				{
					putMarkers(p, 3);
				}
			}
			*/
			
			List<Points> sortedPList = sortPList(interPList, pArc1);
			
			List<Points> checkPList = new ArrayList<Points>();
			checkPList.add(pArc1);
			
			if(sortedPList.size() > 0)
				checkPList.addAll(sortedPList);
			
			checkPList.add(pArc2);		
			
			boolean bCheckP1 = checkPointBlocked(pArc1);
			
			//JOptionPane.showMessageDialog(null, interPList.size() + " : " + bCheckP1);
			
					
			if(bCheckP1)
			{
				for(int x = 1; (x+1) < checkPList.size();)
				{
					LineSegement freeAS = new LineSegement(checkPList.get(x), checkPList.get(x+1));
					freeAS.parent = center;
					arcSegList.add(freeAS);
											
					//putMarkers(checkPList.get(x), 4);
					//putMarkers(checkPList.get(x+1), 1);
					
					x += 2;
				}
			}
			else
			{
				for(int x = 0; (x+1) < checkPList.size();)
				{
					LineSegement freeAS = new LineSegement(checkPList.get(x), checkPList.get(x+1));
					freeAS.parent = center;
					arcSegList.add(freeAS);
					
					//putMarkers(checkPList.get(x), 3);
					//putMarkers(checkPList.get(x+1), 1);
					
					x += 2;
				}
			}
			
			return arcSegList;			
		}
		
		public List<Points> getIntersectionInHome(Points center, Points pArc1, Points pArc2, float rad, float tolerance)
		{		
			List<Points> interPList = new ArrayList<Points>();	
			
			for(float[][] fRects : furnRects)
			{
				List<Points> intList = getIntersectionArcPoly(center, rad, fRects, pArc1, pArc2, tolerance);
				interPList.addAll(intList);
			}
			
			//JOptionPane.showMessageDialog(null, interPList.size());
			return interPList;
		}
		
		public boolean checkPointBlocked(Points test)
		{
			boolean bIsInside = false;
			
			for(HomePieceOfFurniture hpf : home.getFurniture())
			{
				String fName = hpf.getName();
				
				if(!markBoxName.contains(fName))
				{
					boolean bCheck1 = hpf.containsPoint(test.x, test.y, FURN_TOLERANCE);
					
					if(bCheck1)
					{
						bIsInside = true;
						break;
					}
				}
			}//JOptionPane.showMessageDialog(null, "1 :" +  bIsInside);
			
			if(!bIsInside)
			{
				for(Wall w : home.getWalls())
				{
					boolean bCheck2 = w.containsPoint(test.x, test.y, FURN_TOLERANCE);
					
					if(bCheck2)
					{
						bIsInside = true;
						break;
					}
				}//JOptionPane.showMessageDialog(null, "2 :" +  bIsInside);
			}
			
			if(!bIsInside)
			{
				boolean bCheck3 = room.containsPoint(test.x, test.y, ROOM_TOLERANCE);
			
				if(!bCheck3)
				{
					bIsInside = true;
				}//JOptionPane.showMessageDialog(null, "3 :" +  bIsInside);
			}
				
			return bIsInside;
		}
		
		public boolean checkPointBlockedInRoom(Points test, Room r)
		{
			boolean bIsInside = false;
			
			for(HomePieceOfFurniture hpf : home.getFurniture())
			{
				String fName = hpf.getName();
				
				if(!markBoxName.contains(fName) && !(fName.toLowerCase().startsWith(DiningRectPlaceStr)))
				{
					boolean bCheck1 = hpf.containsPoint(test.x, test.y, FURN_TOLERANCE);
					
					if(bCheck1)
					{
						bIsInside = true;
						break;
					}
				}
			}//JOptionPane.showMessageDialog(null, "1 :" +  bIsInside);
			
			if(!bIsInside)
			{
				for(Wall w : home.getWalls())
				{
					boolean bCheck2 = w.containsPoint(test.x, test.y, FURN_TOLERANCE);
					
					if(bCheck2)
					{
						bIsInside = true;
						break;
					}
				}//JOptionPane.showMessageDialog(null, "2 :" +  bIsInside);
			}
			
			if(!bIsInside)
			{
				boolean bCheck3 = r.containsPoint(test.x, test.y, ROOM_TOLERANCE);
			
				if(!bCheck3)
				{
					bIsInside = true;
				}//JOptionPane.showMessageDialog(null, "3 :" +  bIsInside);
			}
				
			return bIsInside;
		}
		
		// ======================= INIT FUNCTIONS ======================= //

		public void getDiningRect()
		{			
			for(HomePieceOfFurniture hpf : home.getFurniture())
			{			
				if(hpf.getName().equalsIgnoreCase("diningrect"))
				{
					diningRect = hpf;
					break;
				}
			}
		}

		public void getDiningRoom()
		{			
			for(Room r : home.getRooms())
			{			
				String roomName = r.getName();
				
				if((roomName != null) && (roomName.equalsIgnoreCase("dining")))
				{
					diningRoom = r;
					break;
				}
			}
		}
		
		public void storeAllFurnRects(Home h)
		{			
			for(HomePieceOfFurniture hp: h.getFurniture())
			{
				String fName = hp.getName();
				//JOptionPane.showMessageDialog(null, fName);
				
				furnIds.add(fName);
				furnRects.add(hp.getPoints());
				furnThicks.add(0.0f);
				furnRectsAccess.add(hp.getPoints());
			}
		}
				
		public void storeAllWallRects(Home h)
		{
			int wallCount = 1;
			
			for(Wall w: h.getWalls())
			{
				furnIds.add("wall_" + wallCount);				
				float[][] wRect = w.getPoints();
				
				List<Points> validPoints = new ArrayList<Points>();
						
				for(int ws = 0; ws < wRect.length; ws++)
				{
					Points p = new Points(wRect[ws][0], wRect[ws][1]);
					
					if(room.containsPoint(p.x, p.y, (ROOM_TOLERANCE * w.getThickness())))
						validPoints.add(p);
				}
				
				//JOptionPane.showMessageDialog(null, wallCount + ":" + validPoints.size());
						
				float[][] validRect = new float[validPoints.size()][2];
				
				for(int i = 0; i < validPoints.size(); i++)
				{
					validRect[i][0] = validPoints.get(i).x;
					validRect[i][1] = validPoints.get(i).y;
				}
				
				furnRects.add(validRect);
				furnThicks.add(w.getThickness());		
				furnRectsAccess.add(validRect);
				
				wallCount++;
			}
		}
		
		public Points getStartingPoint()
		{
			Points startPoints = new Points();
			
			for(HomePieceOfFurniture hpf : home.getFurniture())
			{
				if(hpf.getName().equalsIgnoreCase("Kitchen Door"))
				{
					startPoints = new Points(hpf.getX(), hpf.getY());
				}
			}
			
			return startPoints;
		}
		
		public List<LineSegement> calcStartArcPoints(Points center, float radius, float tolr)
		{
			List<LineSegement> finalList = new ArrayList<LineSegement>();
			
			float[][] diningPoints = diningRoom.getPoints();
			
			// List of Dining border line segments
			List<LineSegement> lsList = new ArrayList<LineSegement>();
			
			for(int f = 0; f < diningPoints.length; f++)
			{
				Points startLS = new Points(diningPoints[f][0], diningPoints[f][1]);					
				Points endLS = null;
				
				if(f == (diningPoints.length - 1))
					endLS = new Points(diningPoints[0][0], diningPoints[0][1]);
				else
					endLS = new Points(diningPoints[f+1][0], diningPoints[f+1][1]);
				
				lsList.add(new LineSegement(startLS, endLS));
				//putMarkers(startLS, 5);
				//putMarkers(endLS, 3);
			}
			
			// List of intersection points with circle
			List<ChordPoints> interPList = new ArrayList<ChordPoints>();
			 
			for(int l = 0; l < lsList.size(); l++)
			{
				List<Points> interP = getIntersectionCircleLine(center, radius, lsList.get(l).startP, lsList.get(l).endP);
				
				//JOptionPane.showMessageDialog(null, center.y + "," +  radius + "," +  lsList.get(l).startP.x + "," +  lsList.get(l).endP.x);
				
				for(Points p : interP)
				{
					boolean bInBetween = checkPointInBetween(p, lsList.get(l).startP, lsList.get(l).endP, tolr);
					
					if(bInBetween)
					{
						interPList.add(new ChordPoints(p, l));
						//putMarkers(p, 5);
					}
				}
			}
			
			// List of chords
			List<ChordSegements> chordList = new ArrayList<ChordSegements>();
			
			if(interPList.size() == 2)
			{
				Points startC = interPList.get(0).p;
				int sIndx = interPList.get(0).lsIndx;
				
				Points endC = interPList.get(1).p;;
				int eIndx = interPList.get(1).lsIndx;
				
				chordList.add(new ChordSegements(startC, endC, sIndx, eIndx));
				//putMarkers(startC, 1);
				//putMarkers(endC, 0);
			}
			else
			{
				for(int i = 0; i < interPList.size(); i++)
				{
					Points startC = interPList.get(i).p;
					int sIndx = interPList.get(i).lsIndx;
					
					Points endC = null;
					int eIndx = -1;
					
					if(i == (interPList.size() - 1))
					{
						endC = interPList.get(0).p;
						eIndx = interPList.get(0).lsIndx;
					}
					else
					{
						endC = interPList.get(i+1).p;
						eIndx = interPList.get(i+1).lsIndx;
					}
					
					chordList.add(new ChordSegements(startC, endC, sIndx, eIndx));	
					
					//putMarkers(startC, 1);
					//putMarkers(endC, 0);
				}
			}
			
			// Final Check			
			for(int c = 0; c < chordList.size(); c++)
			{
				boolean bCollect = false;
				ChordSegements cs = chordList.get(c);
				
				for(int l = 0; l < lsList.size(); l++)
				{
					if((l != cs.startLSIndx) && (l != cs.endLSIndx))
					{
						LineSegement ls = new LineSegement(cs.startP, cs.endP);
						Points midP = new Points(((cs.startP.x + cs.endP.x)/2),((cs.startP.y + cs.endP.y)/2));
						
						Intersect inter = getIntersectPoint(ls, lsList.get(l));
						
						//JOptionPane.showMessageDialog(null, inter.p.x + "," + inter.p.y + " : " + inter.dist);
						
						if((inter.dist == INFINITY_COORDS) && diningRoom.containsPoint(midP.x, midP.y, ROOM_TOLERANCE))
						{
							bCollect = true;
						}
						else
							bCollect = false;
					}
				}
				
				if(bCollect)
				{
					finalList.add(new LineSegement(cs.startP, cs.endP));
					//JOptionPane.showMessageDialog(null, cs.startP.x + "," + cs.startP.y + " | " + cs.endP.x + "," + cs.endP.y);
					
					//putMarkers(cs.startP, 1);
					//putMarkers(cs.endP, 0);
				}
			}
			
			return finalList;
		}
		
		// ======================= UTILITY FUNCTIONS ======================= //
		
		public HomePieceOfFurniture getFurnItem(String furnName)
		{
			HomePieceOfFurniture matchFurn = null;
			List<FurnitureCategory> fCatg = getUserPreferences().getFurnitureCatalog().getCategories();		

			try 
			{
				for(int c = 0; c < fCatg.size(); c++ )
				{
					List<CatalogPieceOfFurniture> catPOF = fCatg.get(c).getFurniture();

					for(int p = 0; p < catPOF.size(); p++ )
					{
						if(furnName.equalsIgnoreCase(catPOF.get(p).getName()))
						{
							matchFurn = new HomePieceOfFurniture(catPOF.get(p));
							//JOptionPane.showMessageDialog(null, "Found " + furnName);
							break;
						}
					}	
				}				
			}
			catch(Exception e){e.printStackTrace();}

			return matchFurn;
		}
				
		public boolean checkIntersectWithAllFurns(HomePieceOfFurniture hpf, boolean bAddAccessibility)
		{
			boolean bIntersects = false;
			
			for(int x = 0 ; x < furnIds.size(); x++)
			{
				if(!hpf.getName().equalsIgnoreCase(furnIds.get(x)))
				{	
					float[][] refFurnRect = furnRects.get(x);
					
					for(int f = 0; f < refFurnRect.length; f++)
					{
						Points startLine = new Points(refFurnRect[f][0], refFurnRect[f][1]);
						
						Points endLine = null;
						
						if(f == (refFurnRect.length - 1))
							endLine = new Points(refFurnRect[0][0], refFurnRect[0][1]);
						else
							endLine = new Points(refFurnRect[f+1][0], refFurnRect[f+1][1]);				
						
						LineSegement ls = new LineSegement(startLine, endLine);
						
						// For Accessibility check
						List<Intersect> interList = new ArrayList<Intersect>();
						
						if(bAddAccessibility)
							interList = checkIntersectAccessibility(ls, hpf.getName());
						else
							interList = checkIntersect(ls, hpf.getName());
						
						for(Intersect inter : interList)
						{
							if(inter != null)
							{
								bIntersects = checkPointInBetween(inter.p, ls.startP, ls.endP, FURN_TOLERANCE);
								
								if(bIntersects)
									break;
							}
							//putMarkers(inter.p, 3);
						}
					}
					
					if(bIntersects)
						break;
				}
				
				if(bIntersects)
					break;
			}
			
			return bIntersects;
		}
		
		public boolean checkInsideRoom(Room r, float[][] fRect)
		{
			boolean bLiesInside = false;
			
			for(int f = 0; f < fRect.length; f++)
			{
				bLiesInside = r.containsPoint(fRect[f][0], fRect[f][1], ROOM_TOLERANCE);
			}
			
			return bLiesInside;
		}
		
		public List<Intersect> checkIntersect(LineSegement r, String furnId)
		{
			List<Intersect> interList = new ArrayList<Intersect>();
			
			Intersect inter = null;
			int indx = -1;
			
			if((indx = furnIds.indexOf(furnId)) > -1)
			{ 				
				float[][] fRect = furnRects.get(indx);
				//float[][] fRect = furnRectsBloated.get(indx);
						
				if(fRect.length == 2)
				{
					LineSegement l1 = new LineSegement((new Points(fRect[0][0], fRect[0][1])) , (new Points(fRect[1][0], fRect[1][1])));
					
					inter = getIntersectPoint(r, l1);				
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("1. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
				}
				else if(fRect.length == 4)
				{
					LineSegement l1 = new LineSegement((new Points(fRect[0][0], fRect[0][1])) , (new Points(fRect[1][0], fRect[1][1])));
					LineSegement l2 = new LineSegement((new Points(fRect[1][0], fRect[1][1])) , (new Points(fRect[2][0], fRect[2][1])));
					LineSegement l3 = new LineSegement((new Points(fRect[2][0], fRect[2][1])) , (new Points(fRect[3][0], fRect[3][1])));
					LineSegement l4 = new LineSegement((new Points(fRect[3][0], fRect[3][1])) , (new Points(fRect[0][0], fRect[0][1])));
					
					inter = getIntersectPoint(r, l1);				
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("1. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
					
					inter = getIntersectPoint(r, l2);				
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("2. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
					
					inter = getIntersectPoint(r, l3);
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("3. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
					
					inter = getIntersectPoint(r, l4);
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("4. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
				}
				//JOptionPane.showMessageDialog(null, debug);					
			}
			
			return interList;
		}
		
		public List<Intersect> checkIntersectAccessibility(LineSegement r, String furnId)
		{
			List<Intersect> interList = new ArrayList<Intersect>();
			
			Intersect inter = null;
			int indx = -1;
			
			if((indx = furnIds.indexOf(furnId)) > -1)
			{ 				
				float[][] fRect = furnRectsAccess.get(indx);
						
				if(fRect.length == 2)
				{
					LineSegement l1 = new LineSegement((new Points(fRect[0][0], fRect[0][1])) , (new Points(fRect[1][0], fRect[1][1])));
					
					inter = getIntersectPoint(r, l1);				
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("1. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
				}
				else if(fRect.length == 4)
				{
					LineSegement l1 = new LineSegement((new Points(fRect[0][0], fRect[0][1])) , (new Points(fRect[1][0], fRect[1][1])));
					LineSegement l2 = new LineSegement((new Points(fRect[1][0], fRect[1][1])) , (new Points(fRect[2][0], fRect[2][1])));
					LineSegement l3 = new LineSegement((new Points(fRect[2][0], fRect[2][1])) , (new Points(fRect[3][0], fRect[3][1])));
					LineSegement l4 = new LineSegement((new Points(fRect[3][0], fRect[3][1])) , (new Points(fRect[0][0], fRect[0][1])));
					
					inter = getIntersectPoint(r, l1);				
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("1. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
					
					inter = getIntersectPoint(r, l2);				
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("2. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
					
					inter = getIntersectPoint(r, l3);
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("3. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
					
					inter = getIntersectPoint(r, l4);
					if(inter.dist < INFINITY_COORDS)
						interList.add(inter);
					
					//debug += ("4. " + inter.p.x + "," + inter.p.y + " -> " + inter.dist + "\n");
				}
				//JOptionPane.showMessageDialog(null, debug);					
			}
			
			return interList;
		}
		
		public Intersect getIntersectPoint(LineSegement ref, LineSegement l)
		{
			Intersect inter = new Intersect((new Points()), 0.0f);
			
			float A = (ref.endP.y - ref.startP.y);											// (y2 - y1)
			float B = (ref.startP.x - ref.endP.x);											// (x1 - x2)		
			float C = ((ref.endP.y * ref.startP.x) - (ref.startP.y * ref.endP.x));			// (y2x1 - y1x2)
			
			float P = (l.endP.y - l.startP.y);												// (y2' - y1')
			float Q = (l.startP.x - l.endP.x);												// (x1' - x2')		
			float R = ((l.endP.y * l.startP.x) - (l.startP.y * l.endP.x));					// (y2'x1' - y1'x2')
			
			float yNum = (P*C - R*A);
			float yDen = (P*B - Q*A);

			float xNum = (Q*C - R*B);
			float xDen = (Q*A - P*B);
			
			if((xDen == 0.0f) || (yDen == 0.0f))
			{
				inter.p = new Points(2*INFINITY_COORDS, 2*INFINITY_COORDS);
				inter.dist = INFINITY_COORDS;
			}
			else
			{
				inter.p = new Points((xNum/xDen), (yNum/yDen));				
				boolean bC1 = checkPointInBetween(inter.p, l.startP, l.endP, FURN_TOLERANCE);
				
				//JOptionPane.showMessageDialog(null, bC1 + " /  Intersection -> X : " + inter.p.x + ", Y : " + inter.p.y);
				
				if(bC1)
				{		
					inter.dist = calcDistance(inter.p, ref.startP);					
				}
				else
				{
					inter.p = new Points(INFINITY_COORDS, INFINITY_COORDS);
					inter.dist = INFINITY_COORDS;
				}
			}
			
			return inter;			
		}
		
		public List<LineSegement> getInnerWallSegements(Home h)
		{	
			List<LineSegement> wsList = new ArrayList<LineSegement>();
			
			for(Wall w: h.getWalls())
			{			
				float[][] wRect = w.getPoints();				
				List<Points> validPoints = new ArrayList<Points>();
						
				for(int ws = 0; ws < wRect.length; ws++)
				{
					Points p = new Points(wRect[ws][0], wRect[ws][1]);
					
					if(room.containsPoint(p.x, p.y, (ROOM_TOLERANCE * w.getThickness())))
						validPoints.add(p);
				}
						
				if(validPoints.size() == 2)
				{
					LineSegement ws = new LineSegement(validPoints.get(0), validPoints.get(1));
					wsList.add(ws);
				}
			}
			
			return wsList;
		}
		
		public int getQuadrantInfo(Points p)
		{
			int qIndx = 0;
			
			if((p.x > 0.0f) && (p.y > 0.0f))
				qIndx = 1;
			else if((p.x < 0.0f) && (p.y > 0.0f))
				qIndx = 2;
			else if((p.x < 0.0f) && (p.y < 0.0f))
				qIndx = 3;
			else if((p.x > 0.0f) && (p.y < 0.0f))
				qIndx = 4;
			
			return qIndx;
		}
		
		public void placeFurnItem(HomePieceOfFurniture inFurn, FurnLoc fLoc)
		{
			HomePieceOfFurniture outFurn = inFurn;
			outFurn.setName(inFurn.getName());
			outFurn.setWidth(fLoc.w);
			outFurn.setAngle(fLoc.ang);
			outFurn.setX(fLoc.p.x);
			outFurn.setY(fLoc.p.y);
			
			home.addPieceOfFurniture(outFurn);
		}

		public void chkFurnOrient(HomePieceOfFurniture furn, LineSegement ws)
		{			
			float[][] fRect = furn.getPoints();
			Points furnBottMid = new Points(((fRect[2][0] + fRect[3][0]) / 2),  ((fRect[2][1] + fRect[3][1]) / 2));
			
			Points wsMid = new Points(((ws.startP.x + ws.endP.x) / 2),  ((ws.startP.y + ws.endP.y) / 2));
			
			float dist = calcDistance(furnBottMid, wsMid);
			//JOptionPane.showMessageDialog(null, "dist : " + dist);
			
			if(dist > ORIENTATION_TOLERANCE)
			{
				furn.setAngle((float)Math.PI);
				//JOptionPane.showMessageDialog(null, "180 rotation");
			}
		}
		
		public Points calcFurnMids(Points p1, Points p2, float d)
		{
			Points retPoints = null;
			
			float l = calcDistance(p1,p2);
			float r = (float)Math.sqrt((d*d) + (0.25f*l*l));
			
			float e = (p2.x - p1.x);
			float f = (p2.y - p1.y);
			float p = (float)Math.sqrt((e*e + f*f));
			float k = (0.5f * p);
			
			float x1 = p1.x + (e*k/p) + (f/p)*((float)Math.sqrt((r*r - k*k)));
			float y1 = p1.y + (f*k/p) - (e/p)*((float)Math.sqrt((r*r - k*k)));
			
			float x2 = p1.x + (e*k/p) - (f/p)*((float)Math.sqrt((r*r - k*k)));
			float y2 = p1.y + (f*k/p) + (e/p)*((float)Math.sqrt((r*r - k*k)));
			
			// Check for inRoom
			if(room.containsPoint(x1, y1, 0.0f))
			{
				retPoints = new Points(x1, y1);
			}
			else if(room.containsPoint(x2, y2, 0.0f))
			{
				retPoints = new Points(x2, y2);
			}
			
			return retPoints;
					
			/*
			 	Let the centers be: (a,b), (c,d)
				Let the radii be: r, s
					
				  e = c - a                          [difference in x coordinates]
				  f = d - b                          [difference in y coordinates]
				  p = sqrt(e^2 + f^2)                [distance between centers]
				  k = (p^2 + r^2 - s^2)/(2p)         [distance from center 1 to line joining points of intersection]
				   
				                                      
				  x = a + ek/p + (f/p)sqrt(r^2 - k^2)
				  y = b + fk/p - (e/p)sqrt(r^2 - k^2)
				OR
				  x = a + ek/p - (f/p)sqrt(r^2 - k^2)
				  y = b + fk/p + (e/p)sqrt(r^2 - k^2)		
			*/
		}
		
		public List<Points> sortPList(List<Points> interPList, Points ref)
		{
			List<Points> retPList = new ArrayList<Points>();
			TreeMap<Float, Points> pMap = new TreeMap<Float, Points>();
			
			for(Points p : interPList)
			{
				float dist = calcDistance(p, ref);
				pMap.put(dist, p);
			}
			
			Set<Float> keys = pMap.keySet();
			
			for(Float d : keys)
			{
				retPList.add(pMap.get(d));
			}
					
			return retPList;
		}
		
		public HomePieceOfFurniture addAccesibilityRect(HomePieceOfFurniture hp, float top, float right, float bottom, float left)
		{
			float w = hp.getWidth();
			float d = hp.getDepth();
			
			hp.setWidth(w + right + left);
			hp.setDepth(d + top + bottom);
			
			return hp;
		}
		
		public void addAccesibilityRectTemp(HomePieceOfFurniture hp)
		{
			float w = hp.getWidth();
			float d = hp.getDepth();
			
			hp.setWidth(2*w);
			hp.setDepth(2*d);
		}
		
		public List<Points> getIntersectionArcPoly(Points center, float rad, float[][] furnRect, Points arcP1, Points arcP2, float tolerance)
		{
			List<Points> retList = new ArrayList<Points>();			
			List<LineSegement> lsList = new ArrayList<LineSegement>();
			
			//JOptionPane.showMessageDialog(null,("furn : " + furnRect[0][0] + "," + furnRect[0][1] + " / " + furnRect[1][0] + "," + furnRect[1][1] + " / " + furnRect[2][0] + "," + furnRect[2][1] + " / " + furnRect[3][0] + "," + furnRect[3][1]));
			
			if(furnRect.length == 2)
			{
				Points startLine = new Points(furnRect[0][0], furnRect[0][1]);
				Points endLine = new Points(furnRect[1][0], furnRect[1][1]);
				
				LineSegement ls = new LineSegement(startLine, endLine);
				lsList.add(ls);
			}
			else
			{			
				for(int f = 0; f < furnRect.length; f++)
				{
					Points startLine = new Points(furnRect[f][0], furnRect[f][1]);
					
					Points endLine = null;
					
					if(f == (furnRect.length - 1))
						endLine = new Points(furnRect[0][0], furnRect[0][1]);
					else
						endLine = new Points(furnRect[f+1][0], furnRect[f+1][1]);				
					
					LineSegement ls = new LineSegement(startLine, endLine);
					lsList.add(ls);
				}
			}
			
			for(int l = 0; l < lsList.size(); l++)
			{
				Points startP = lsList.get(l).startP;
				Points endP = lsList.get(l).endP;
				
				List<Points> interP = getIntersectionArcLineSeg(center, rad, startP, endP, arcP1, arcP2);
				
				for(Points inter : interP)
				{		
					boolean bInBetween = checkPointInBetween(inter, startP, endP, tolerance);
					
					if(bInBetween)
					{
						retList.add(inter);
						//putMarkers(inter, false);
					}
				}									
			}
			
			return retList;
		}
		
		public List<Points> getIntersectionArcLineSeg(Points center, float rad, Points startL, Points endL, Points arcP1, Points arcP2)
		{
			List<Points> retList = new ArrayList<Points>();
			
			List<Points> interList = getIntersectionCircleLine(center, rad, startL, endL);
			
			for(Points p : interList)
			{
				boolean bOnSameSide = checkPointOnSameSide(center, p, arcP1, arcP2);
				
				//JOptionPane.showMessageDialog(null, ">>--->>" + bOnSameSide);
				
				if(!bOnSameSide)
					retList.add(p);
			}		
			
			return retList;
		}	
		
		public List<Points> getIntersectionArcLineSeg2(Points center, float rad, float slope, float intercept, Points arcP1, Points arcP2)
		{
			List<Points> retList = new ArrayList<Points>();
			
			//JOptionPane.showMessageDialog(null, (center.x + "," + center.y + " ; " + rad + " ; " + slope + " ; " + intercept)); 
			
			List<Points> interList = getIntersectionCircleLine2(center, rad, slope, intercept);
			
			for(Points p : interList)
			{
				//putMarkers(p, 0);
				
				boolean bOnSameSide = checkPointOnSameSide(center, p, arcP1, arcP2);
				
				//JOptionPane.showMessageDialog(null, ">>>" + bOnSameSide);
						
				if(!bOnSameSide)
					retList.add(p);
			}		
			
			return retList;
		}		
		
		public List<Points> getIntersectionCircleLine(Points center, float rad, Points startL, Points endL)
		{
			List<Points> interList = new ArrayList<Points>();
			
			try
			{	
				if(Math.abs(endL.x - startL.x) < tolerance)
				{
					float dist = (float) Math.abs(startL.x - center.x);
							
					if(dist <= rad)
					{
						float x01 = startL.x;
						float y01 = center.y - (float)Math.sqrt((rad*rad) - (dist*dist));
						
						Points inter1 = new Points(x01, y01);
						interList.add(inter1);
						//putMarkers(inter1, false);
						
						float x02 = startL.x;
						float y02 = center.y + (float)Math.sqrt((rad*rad) - (dist*dist));
						
						Points inter2 = new Points(x02, y02);
						interList.add(inter2);
						//putMarkers(inter2, false);
					}
					//else : Line does not intersect with this circle
				}
				else
				{
					// Equation of Line
					float m = ((endL.y - startL.y) / (endL.x - startL.x));
					float c = startL.y - (m*startL.x);
					
					// (m^2+1)x^2 + 2(mc−mq−p)x + (q^2−r^2+p^2−2cq+c^2) = 0			
					
					float A = (m*m) + 1;
					float B = 2*((m*c) - (m*center.y) - center.x);
					float C = (center.y*center.y) - (rad*rad) + (center.x*center.x) - 2*(c*center.y) + (c*c);
					
					float D = (B*B) - 4*A*C;
					
					if(D == 0)
					{
						float x1 = ((-B) + (float)Math.sqrt(D)) / (2*A);
						float y1 = (m*x1) + c;
						
						Points inter = new Points(x1, y1);
						interList.add(inter);	
						
						//putMarkers(inter, true);
					}
					else if (D > 0)
					{
						float x1 = ((-B) + (float)Math.sqrt(D)) / (2*A);
						float y1 = (m*x1) + c;
						
						Points inter1 = new Points(x1, y1);
						interList.add(inter1);
						
						//putMarkers(inter1, false);
						
						float x2 = ((-B) - (float)Math.sqrt(D)) / (2*A);
						float y2 = (m*x2) + c;
						
						Points inter2 = new Points(x2, y2);
						interList.add(inter2);
						
						//putMarkers(inter2, false);
					}
				}				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -xxxxx- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
			
			return interList;
		}
		
		public List<Points> getIntersectionCircleLine2(Points center, float rad, float slope, float intercept)
		{
			List<Points> interList = new ArrayList<Points>();
			
			try
			{	
				// Equation of Line
				float m = slope;
				float c = intercept;
				
				// (m^2+1)x^2 + 2(mca��mq−p)x + (q^2−r^2+p^2−2cq+c^2) = 0			
				
				float A = (m*m) + 1;
				float B = 2*((m*c) - (m*center.y) - center.x);
				float C = (center.y*center.y) - (rad*rad) + (center.x*center.x) - 2*(c*center.y) + (c*c);
				
				float D = (B*B) - 4*A*C;
				
				if(D == 0)
				{
					float x1 = ((-B) + (float)Math.sqrt(D)) / (2*A);
					float y1 = (m*x1) + c;
					
					Points inter = new Points(x1, y1);
					interList.add(inter);	
					
					//putMarkers(inter, true);
				}
				else if (D > 0)
				{
					float x1 = ((-B) + (float)Math.sqrt(D)) / (2*A);
					float y1 = (m*x1) + c;
					
					Points inter1 = new Points(x1, y1);
					interList.add(inter1);
					
					//putMarkers(inter1, false);
					
					float x2 = ((-B) - (float)Math.sqrt(D)) / (2*A);
					float y2 = (m*x2) + c;
					
					Points inter2 = new Points(x2, y2);
					interList.add(inter2);
					
					//putMarkers(inter2, false);
				}		
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -xxxxx- EXCEPTION : " + e.getMessage()); 
				e.printStackTrace();
			}
			
			return interList;
		}
		
		public List<Points> getIntersectionArcCircle(Points centerC, float radC, Points arcP1, Points arcP2, Points centerArc, float radArc)
		{
			List<Points> retList = new ArrayList<Points>();
			
			List<Points> interList = getIntersectionTwoCircle(centerC, radC, centerArc, radArc);
			
			for(Points p : interList)
			{				
				boolean bOnSameSide = checkPointOnSameSide(centerArc, p, arcP1, arcP2);
				
				if(!bOnSameSide)
				{
					retList.add(p);
				}
			}		
			
			return retList;
		}
		
		public List<Points> getIntersectionTwoCircle(Points centerC, float radC, Points centerArc, float radArc)
		{
			List<Points> retList = new ArrayList<Points>();
			
			float dist = Math.abs(calcDistance(centerC, centerArc));
			float diffR = Math.abs(radC - radArc);
			float sumR = Math.abs(radC + radArc);
			
			// If circles intersect, compute the points of intersection
			if((dist >= diffR) && (dist <= sumR))
			{
				float d = (float)Math.sqrt(((centerC.x - centerArc.x)*(centerC.x - centerArc.x)) + ((centerC.y - centerArc.y)*(centerC.y - centerArc.y)));
				float l = ((radC*radC) - (radArc*radArc) + (d*d)) / (2*d);
				float h = (float)Math.sqrt((radC*radC) - (l*l));
				
				float x1 = ((l/d)*(centerArc.x - centerC.x)) + ((h/d)*(centerArc.y - centerC.y)) + centerC.x;
				float y1 = ((l/d)*(centerArc.y - centerC.y)) - ((h/d)*(centerArc.x - centerC.x)) + centerC.y;			
				Points inter1 = new Points(x1, y1);
				retList.add(inter1);
				
				float x2 = ((l/d)*(centerArc.x - centerC.x)) - ((h/d)*(centerArc.y - centerC.y)) + centerC.x;
				float y2 = ((l/d)*(centerArc.y - centerC.y)) + ((h/d)*(centerArc.x - centerC.x)) + centerC.y;
				Points inter2 = new Points(x2, y2);
				retList.add(inter2);		
			}

			return retList;
		}
		
		public boolean checkPointOnSameSide(Points a, Points b, Points pS1, Points pS2)
		{
			boolean bRet = false;
			
			// ((y1−y2)(ax−x1)+(x2−x1)(ay−y1))((y1−y2)(bx−x1)+(x2−x1)(by−y1)) < 0
			
			float res = ( ((pS1.y - pS2.y)*(a.x - pS1.x)) + ((pS2.x - pS1.x)*(a.y - pS1.y)) )*( ((pS1.y - pS2.y)*(b.x - pS1.x)) + ((pS2.x - pS1.x)*(b.y - pS1.y)) );
			
			if(res < 0)
				bRet = false;
			else
				bRet = true;
			
			return bRet;
		}
		
		public boolean checkPointInBetween(Points test, Points start, Points end, float tolPercent)
		{
			boolean bRet = false;
			
			float distST = calcDistance(start, test);
			float distTE = calcDistance(test, end);
			float distSE = calcDistance(start, end);
			
			float distSEAbs = (float)(Math.abs(distST + distTE - distSE));
					
			if(distSEAbs <= tolPercent)
				bRet = true;
			
			return bRet;			
		}
		
		public float calcDistance(Points p1, Points p2)
		{
			float d = (float) Math.sqrt(((p2.x - p1.x) * (p2.x - p1.x)) + ((p2.y - p1.y)*(p2.y - p1.y)));
			return d;
		}
		
		public float[][] genAccessBox(HomePieceOfFurniture hpf, float width, float depth)
		{
			HomePieceOfFurniture hpfC = hpf.clone();
			hpfC.setWidth(hpf.getWidth() + (2*width));
			hpfC.setDepth(hpf.getDepth() + (2*depth));
			
			float[][] accessRect = hpfC.getPoints();
			
			return accessRect;
		}
		
		public void putMarkerOnPolygon(float[][] polyRect, int indx)
		{
			for(int x = 0 ; x < polyRect.length; x++)
			{
				Points p = new Points(polyRect[x][0], polyRect[x][1]);
				putMarkers(p, indx);
			}
		}
		
		// ======================= DEBUG FUNCTIONS ======================= //

		public void putMarkers(Points p, int indx)
		{
			HomePieceOfFurniture box = null;

			box = markBoxes[indx].clone();			
			box.setX(p.x);
			box.setY(p.y);
			home.addPieceOfFurniture(box);
		}

		public HomePieceOfFurniture[] getMarkerBoxes()
		{
			HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[MARKBOX_COUNT];
			int count = 0;

			List<FurnitureCategory> fCatg = getUserPreferences().getFurnitureCatalog().getCategories();

			for(int c = 0; c < fCatg.size(); c++ )
			{
				if(count >= MARKBOX_COUNT)
					break;

				List<CatalogPieceOfFurniture> catPOF = fCatg.get(c).getFurniture();

				for(int p = 0; p < catPOF.size(); p++ )
				{
					if(catPOF.get(p).getName().equals("boxred"))
					{
						markBoxes[0] = new HomePieceOfFurniture(catPOF.get(p));
						markBoxName.add("boxred");
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxgreen"))
					{
						markBoxes[1] = new HomePieceOfFurniture(catPOF.get(p));
						markBoxName.add("boxgreen");
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxblue"))
					{
						markBoxes[2] = new HomePieceOfFurniture(catPOF.get(p));
						markBoxName.add("boxblue");
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxyellow"))
					{
						markBoxes[3] = new HomePieceOfFurniture(catPOF.get(p));
						markBoxName.add("boxyellow");
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxteal"))
					{
						markBoxes[4] = new HomePieceOfFurniture(catPOF.get(p));
						markBoxName.add("boxteal");
						count++;
					}
					else if(catPOF.get(p).getName().equals("boxblack"))
					{
						markBoxes[5] = new HomePieceOfFurniture(catPOF.get(p));
						markBoxName.add("boxblack");
						count++;
					}

					if(count >= MARKBOX_COUNT)
						break;
				}	
			}

			return markBoxes;
		}
		
	}

	@Override
	public PluginAction[] getActions() 
	{
		return new PluginAction [] {new RoomTestAction()}; 
	}
}
