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
	public List<float[][]> furnRectsBloated = new ArrayList<float[][]>();

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
		
		public float TOLERANCE_PERCENT = 0.02f;		//0.05f;		
		public float INFINITY_COORDS = 10000.0f;
		
		public float FURNITURE_BLOAT_SIZE = 2.0f;	// 2cm
		
		public boolean bShowMarker = true;
		public boolean bShowPathway = true;
		
		public String refFurnId = "";
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
	
				//getDiningRect();
				getDiningRoom();
				
				markBoxes = getMarkerBoxes();
				
				storeAllFurnRects(home);
				storeAllWallRects(home);
				
				// 1 ================================================ //
				/*
				Points startP = new Points(100.0f, 0.0f);
				Points endP = new Points(-100.0f, 0.0f);
				
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 50.0f;
				
				List<Points> circLinePList = getIntersectionCircleLine(centerP, radius, startP, endP);
				
				for(Points p : circLinePList)
				{
					putMarkers(p, 2);
				}
				
				putMarkers(startP, 1);
				putMarkers(endP, 1);
				putMarkers(centerP, 3);
				*/
				
				// 2 ================================================ //
				/*
				Points startP = new Points(400.0f, -100.0f);
				Points endP = new Points(-400.0f, -100.0f);
				
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 200.0f;
				
				Points arcP2 = new Points(-199.0f, -20.0f);
				Points arcP1 = new Points(200.0f, 0.0f);
				
				List<Points> circLineSPList = getIntersectionArcLineSeg(centerP, radius, startP, endP, arcP1, arcP2);
				
				for(Points p : circLineSPList)
				{
					putMarkers(p, 3);
				}
				
				putMarkers(arcP1, 1);
				putMarkers(arcP2, 1);
				putMarkers(centerP, 2);
				
				putMarkers(startP, 4);
				putMarkers(endP, 4);
				*/
				
				// ================================================ //
				/*
				float tolerance = 0.5f;
				
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 200.0f;
				
				Points arcP2 = new Points(-199.0f, -20.0f);
				Points arcP1 = new Points(200.0f, 0.0f);
				
				List<Points> masterFurnArcPList = new ArrayList<Points>();
				
				for(float[][] fRect : furnRects)
				{
					List<Points> furnArcPList = getIntersectionArcRectangle(centerP, radius, fRect, arcP1, arcP2, tolerance);
					masterFurnArcPList.addAll(furnArcPList);
				}
				
				for(Points p : masterFurnArcPList)
				{
					putMarkers(p, 3);
				}
				
				putMarkers(arcP1, 1);
				putMarkers(arcP2, 1);
				putMarkers(centerP, 2);
				*/
				
				// 3 ================================================ //
				/*
				float tolerance = 0.5f;
				
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 300.0f;
				
				Points arcP1 = new Points(173.2f, -173.2f);
				Points arcP2 = new Points(0.0f, 300.0f);

				if(diningRoom != null)
				{
					List<Points> furnArcPList = getIntersectionArcRectangle(centerP, radius, diningRoom.getPoints(), arcP1, arcP2, tolerance);
					
					for(Points p : furnArcPList)
					{
						putMarkers(p, 3);
					}
					
					putMarkers(arcP1, 1);
					putMarkers(arcP2, 1);
					putMarkers(centerP, 2);
				}
				else
					JOptionPane.showMessageDialog(null," No room " );
				*/
				
				// 4 ================================================ //
				/*
				float tolerance = 0.5f;
				
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 300.0f;
				
				Points arcP1 = new Points(173.2f, -173.2f);
				Points arcP2 = new Points(0.0f, 300.0f);
				
				List<Points> masterFurnArcPList = new ArrayList<Points>();
				
				for(float[][] fRect : furnRects)
				{
					List<Points> furnArcPList = getIntersectionArcRectangle(centerP, radius, fRect, arcP1, arcP2, tolerance);
					masterFurnArcPList.addAll(furnArcPList);
				}

				if(diningRoom != null)
				{
					List<Points> diningArcPList = getIntersectionArcRectangle(centerP, radius, diningRoom.getPoints(), arcP1, arcP2, tolerance);
					masterFurnArcPList.addAll(diningArcPList);
				}
				else
					JOptionPane.showMessageDialog(null," No room " );
				
				
				for(Points p : masterFurnArcPList)
				{
					putMarkers(p, 3);
				}
				
				putMarkers(arcP1, 1);
				putMarkers(arcP2, 1);
				putMarkers(centerP, 2);
				*/
				// 5 ================================================ //
				/*
				for(HomePieceOfFurniture hpf : home.getFurniture())
				{					
					if(hpf.getName().equalsIgnoreCase("diningrect"))
					{
						addAccesibilityRectTemp(hpf);
						hpf.setX(100.0f);
						hpf.setY(200.0f);
						
						break;
					}
				}
				*/
				
				// 6 ================================================ //
				/*
				float tolerance = 0.5f;
				
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 100.0f;
				
				Points arcP1 = new Points(0.2f, -102.0f);
				Points arcP2 = new Points(0.0f, 100.0f);

				LineSegement lgfreeArcSeg = getLongestFreeArcSeg(centerP, arcP1, arcP2, radius, tolerance);
				
				if(lgfreeArcSeg!= null)
				{
					putMarkers(lgfreeArcSeg.startP, 4);
					putMarkers(lgfreeArcSeg.endP, 4);
				}
				*/
				
				// 7 ================================================ //
				/*
				LineSegement longestSeg = getLongestSideOfRoom(diningRoom);
				
				if(longestSeg!= null)
				{
					putMarkers(longestSeg.startP, 4);
					putMarkers(longestSeg.endP, 4);
					
					JOptionPane.showMessageDialog(null, calcDistance(longestSeg.startP, longestSeg.endP));
				}
				*/
				
				// 8,9 ================================================ //
				/*
				Points centerP = new Points(0.0f, 0.0f);
				
				List<LineSegement> wsList = getInnerWallSegements(home);
				HomePieceOfFurniture hpf = home.getFurniture().get(0);
				
				for(LineSegement ws : wsList)
				{					
					placeFurnParallelToWall(ws, hpf.clone(), centerP);					
					placeFurnPerpendicularToWall(ws, hpf.clone(), centerP);
					JOptionPane.showMessageDialog(null, "****");
				}
				*/
				// 10 ================================================ //
				/*
				Points centerP = new Points(0.0f, 0.0f);
				float radius = 100.0f;
				
				float intercept = centerP.y; 				
				List<LineSegement> wsList = getInnerWallSegements(home);
				
				int markerCol = 3;
				
				for(LineSegement ws : wsList)
				{
					float angle = calcWallAngles(ws);
					float slope = (float)Math.tan(angle);
					
					List<Points> interPList = getIntersectionCircleLine2(centerP, radius, slope, intercept);
				
					if(markerCol == 3)
						markerCol = 1;
					else if(markerCol == 1)
						markerCol = 3;
						
					for(Points p : interPList)
					{
						putMarkers(p, markerCol);
					}
					
					Points midP = new Points(((ws.startP.x + ws.endP.x)/2), ((ws.startP.y + ws.endP.y)/2));
					putMarkers(midP, 5);
					
					JOptionPane.showMessageDialog(null, "****");
				}
				
				putMarkers(centerP, 0);
				*/
				// 11 ================================================ //
				/*
				HomePieceOfFurniture hpf = home.getFurniture().get(0);
				
				Points centerP = new Points(0.0f, 0.0f);
				putMarkers(centerP, 0);
				
				float distance = 200.0f;				
				List<LineSegement> wsList = getInnerWallSegements(home);
				
				LineSegement ws = wsList.get(0);
				
				List<Points> interPList = calcAllFourFurnCoordinates(ws, centerP, distance);
			
				if(interPList.size() > 3)
				{
					placeFurnParallelToWall(ws, hpf.clone(), interPList.get(0));
					placeFurnParallelToWall(ws, hpf.clone(), interPList.get(1));
					placeFurnPerpendicularToWall(ws, hpf.clone(), interPList.get(2));
					placeFurnPerpendicularToWall(ws, hpf.clone(), interPList.get(3));
				}
				
				Points midP = new Points(((ws.startP.x + ws.endP.x)/2), ((ws.startP.y + ws.endP.y)/2));
				putMarkers(midP, 5);
				
				//JOptionPane.showMessageDialog(null, "****");
				*/
				// 12 ================================================ //
				/*
				List<HomePieceOfFurniture> hpfList = home.getFurniture();
				
				HomePieceOfFurniture hpf = hpfList.get(hpfList.size() - 1);
				refFurnId = hpf.getName();
				
				boolean bIntersects = checkIntersectWithAllFurns(hpf);
				
				JOptionPane.showMessageDialog(null, bIntersects);
				*/
				// 13 ================================================ //
				
				Points centerP = new Points(0.0f, 0.0f);
				putMarkers(centerP, 5);
				
				float radius = 600.0f;
				float tolerance = 0.5f;
				
				calcStartArcPoints(centerP, radius, tolerance);
				
				// ================================================ //

			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				//e.printStackTrace();
			}
		}
		
		public void calcStartArcPoints(Points center, float radius, float tolr)
		{
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
			}
			
			//List of intersection points with circle
			List<ChordPoints> interPList = new ArrayList<ChordPoints>();
			
			for(int l = 0; l < lsList.size(); l++)
			{
				List<Points> interP = getIntersectionCircleLine(center, radius, lsList.get(l).startP, lsList.get(l).endP);
				
				for(Points p : interP)
				{
					boolean bInBetween = checkPointInBetween(p, lsList.get(l).startP, lsList.get(l).endP, tolr);
					
					if(bInBetween)
					{
						interPList.add(new ChordPoints(p, l));
						//putMarkers(p, 3);
					}
				}
			}
			
			// List of chords
			List<ChordSegements> chordList = new ArrayList<ChordSegements>();
			
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
			
			// Final Check	
			List<LineSegement> finalList = new ArrayList<LineSegement>();
			
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
					putMarkers(cs.startP, 1);
					putMarkers(cs.endP, 0);
				}
			}
			
		}
		
		public List<Points> calcAllFourFurnCoordinates(LineSegement ws, Points centerP, float dist) 
		{
			List<Points> retPList = new ArrayList<Points>();
			
			float intercept = centerP.y; 
			
			float slopePara = calcWallAngles(ws);						// Parallel
			
			List<Points> interPList1 = getIntersectionCircleLine2(centerP, dist, slopePara, intercept);
			retPList.addAll(interPList1);
			
			float slopePerp = (-1.0f / slopePara);		// Perpendicular
			
			List<Points> interPList2 = getIntersectionCircleLine2(centerP, dist, slopePerp, intercept);
			retPList.addAll(interPList2);
			
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
			
			if(bShowMarker)
			{	
				for(Points p : interPList)
				{
					putMarkers(p, 3);
				}
			}
						
			List<Points> sortedPList = sortPList(interPList, pArc1);
			
			List<Points> checkPList = new ArrayList<Points>();
			checkPList.add(pArc1);
			
			if(sortedPList.size() > 0)
				checkPList.addAll(sortedPList);
			
			checkPList.add(pArc2);		
			
			boolean bCheckP1 = checkPointBlocked(pArc1);
					
			if(bCheckP1)
			{
				for(int x = 1; (x+1) < checkPList.size();)
				{
					LineSegement freeAS = new LineSegement(checkPList.get(x), checkPList.get(x+1));
					freeAS.parent = center;
					arcSegList.add(freeAS);
											
					putMarkers(checkPList.get(x), 3);
					putMarkers(checkPList.get(x+1), 3);
					
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
					
					putMarkers(checkPList.get(x), 5);
					putMarkers(checkPList.get(x+1), 5);
					
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
				List<Points> intList = getIntersectionArcRectangle(center, rad, fRects, pArc1, pArc2, tolerance);
				interPList.addAll(intList);
			}
			
			JOptionPane.showMessageDialog(null, interPList.size());
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
				
				furnIds.add(fName);
				furnRects.add(hp.getPoints());
				furnThicks.add(0.0f);
				
				HomePieceOfFurniture hClone = hp.clone();
				float d = hp.getDepth();
				float w = hp.getWidth();
				
				hClone.setDepth(d + FURNITURE_BLOAT_SIZE);
				hClone.setWidth(w + FURNITURE_BLOAT_SIZE);
				hClone.setElevation(0.0f);
				
				furnRectsBloated.add(hClone.getPoints());
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
							
				wallCount++;
			}
		}
		
		public float[] getStartingPoints()
		{
			float[] startPoints = new float[4];
			
			for(Room r : home.getRooms())
			{	
				String roomName = (r.getName() != null) ? r.getName().trim() : "";
				
				if(!roomName.isEmpty() && roomName.equalsIgnoreCase("foyer"))
				{
					foyer = r;
					
					float[][] roomRect = r.getPoints();
					
					if(roomRect.length > 1)
					{
						startPoints[0] = roomRect[0][0];
						startPoints[1] = roomRect[0][1];
						startPoints[2] = roomRect[1][0];
						startPoints[3] = roomRect[1][1];						
					}
				}
			}
			
			return startPoints;
		}
		
		// ======================= UTILITY FUNCTIONS ======================= //
		
		public boolean checkIntersectWithAllFurns(HomePieceOfFurniture hpf)
		{
			boolean bIntersects = false;
			List<LineSegement> lsList = new ArrayList<LineSegement>();
			
			float[][] refFurnRect = hpf.getPoints(); 
			
			for(int f = 0; f < refFurnRect.length; f++)
			{
				Points startLine = new Points(refFurnRect[f][0], refFurnRect[f][1]);
				
				Points endLine = null;
				
				if(f == (refFurnRect.length - 1))
					endLine = new Points(refFurnRect[0][0], refFurnRect[0][1]);
				else
					endLine = new Points(refFurnRect[f+1][0], refFurnRect[f+1][1]);				
				
				LineSegement ls = new LineSegement(startLine, endLine);
				lsList.add(ls);
			}
			
			for(LineSegement ls: lsList)			
			{
				for(String furnId : furnIds)
				{
					if(!refFurnId.equalsIgnoreCase(furnId))
					{
						List<Intersect> interList = checkIntersect(ls, furnId);
					
						for(Intersect inter : interList)
						{
							if(inter != null)
							{
								bIntersects = checkPointInBetween(inter.p, ls.startP, ls.endP, FURN_TOLERANCE);
								
								if(bIntersects)
									break;
							}
							putMarkers(inter.p, 3);
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
		
		public List<Points> getIntersectionArcRectangle(Points center, float rad, float[][] furnRect, Points arcP1, Points arcP2, float tolerance)
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
				if(endL.x == startL.x)
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
