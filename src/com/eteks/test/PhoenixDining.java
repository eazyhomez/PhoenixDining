package com.eteks.test;

import java.util.ArrayList;
import java.util.List;

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
		public Room diningRoom = null;

		public HomePieceOfFurniture diningRect = null;		

		public int MARKBOX_COUNT = 6;
		public HomePieceOfFurniture[] markBoxes = new HomePieceOfFurniture[MARKBOX_COUNT];
		
		public float ROOM_TOLERANCE = 0.51f;

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
				
				// ================================================ //
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
				// ================================================ //
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
				// ================================================ //
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
				// ================================================ //
				
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
				
				// ================================================ //
			}
			catch(Exception e)
			{
				JOptionPane.showMessageDialog(null," -x-x-x- EXCEPTION : " + e.getMessage()); 
				//e.printStackTrace();
			}
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
		
		// ======================= UTILITY FUNCTIONS ======================= //
		
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
