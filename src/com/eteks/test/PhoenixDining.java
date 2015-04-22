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
		
		public boolean bShowMarker = true;
		public boolean bShowPathway = true;
		
		// ======================= CLASSES ======================= //

		public class Points
		{
			float x;
			float y;

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
				
				Points centerP = new Points(0.0f, 0.0f);
				
				List<LineSegement> wsList = getInnerWallSegements(home);
				HomePieceOfFurniture hpf = home.getFurniture().get(0);
				
				for(LineSegement ws : wsList)
				{					
					placeFurnParallelToWall(ws, hpf.clone(), centerP);					
					placeFurnPerpendicularToWall(ws, hpf.clone(), centerP);
					JOptionPane.showMessageDialog(null, "****");
				}
				
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
				// ================================================ //
				
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				//e.printStackTrace();
			}
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
				
				if(!fName.equals("boxred") && !fName.equals("boxgreen") )
				{
					//furnList.add(hp);					
					furnIds.add(fName);
					furnRects.add(hp.getPoints());
					furnThicks.add(0.0f);
				}
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
