package edu.asu.commons.foraging.graphics;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;

import javax.media.opengl.GL;
import javax.media.opengl.GLAutoDrawable;

import edu.asu.commons.foraging.util.Tuple2f;
import edu.asu.commons.foraging.util.Tuple2i;

/**
 * The FractalTerrain class represents a terrain. It creates terrain using fractal methods and renders it using 
 * a display list. It uses random mid-point displacement method to approximate fractal Brownian motion 
 * calculations used in fractal terrain generation. The advantage is that this is faster in comparison to the 
 * fractal Brownian motion calculation as the later deals with Fourier series. But the limitation is that it
 * generates less realistic looking terrain features.
 * <p>
 * Random displacement method can be used to generate terrain and other natural phenomena.
 *  
 * @author <a href='deepali.bhagvat@asu.edu'>Deepali Bhagvat</a>
 * @version 
 *
 */
public class FractalTerrain extends GraphicsObject{	
	private static final long serialVersionUID = 3131441642777890754L;
	
	/**
	 * A grid of vertices forming this terrain
	 */
	private Grid vertexGrid = new Grid();	//Point3D
	
	/**
	 * A grid of normals at each terrain vertex 
	 */
	private Grid normalGrid = new Grid();	//Vector3D
	
	/**
	 * A grid storing no. of times each vertex has occured
	 */
	private Grid vertexOccurance = new Grid();//Integer
	
	/**
	 * A grid of texture co-ordinates of each terrain vertex
	 */
	private Grid textureCoordinates = new Grid();//Tuple2d
		
	/**
	 * Elevation of the deepest point of the terrain. Initialized to maximum float value so that it is set to
	 * a correct value when compared with the height of the generated terrain points. 
	 */
	private float minElevation = Float.MAX_VALUE;
	
	/**
	 * Random no. generator to generate terrain points of varying heights
	 */
	private transient Random randomNoGenerator = new Random(10); //Setting seed to 10 to remove randomness in the terrain topology
	
	/**
	 * Display list id. Set to -1 at the beginning
	 */
	protected transient int displayListID = -1;
	
	/**
	 * Creates a new fractal terrain
	 *
	 */
	public FractalTerrain() {
		displayListID = -1;
	}
	
	/**
	 * Creates a new fractal terrain with the specified corner points
	 * @param topLeft co-ordinates of the top, left corner point
	 * @param bottomLeft co-ordinates of the bottom, left corner point
	 * @param topRight co-ordinates of the top, right corner point
	 * @param bottomRight co-ordinates of the bottom, right corner point
	 */
	public FractalTerrain(Point3D topLeft, Point3D bottomLeft, Point3D topRight, Point3D bottomRight) {		
		calculateExtents(topLeft, bottomLeft, topRight, bottomRight);
				
		vertexGrid.addRows(2, 0);
		vertexGrid.insertNode(0, 0, topLeft);
		vertexGrid.insertNode(1, 0, bottomLeft);
		vertexGrid.insertNode(0, 1, topRight);
		vertexGrid.insertNode(1, 1, bottomRight);
	}
	
	/**
	 * Calculates maximum and minimum co-ordinates in the X, Y and Z directions. 
	 * @param topLeft co-ordinates of the top, left corner point
	 * @param bottomLeft co-ordinates of the bottom, left corner point
	 * @param topRight co-ordinates of the top, right corner point
	 * @param bottomRight co-ordinates of the bottom, right corner point
	 */
	private void calculateExtents(Point3D topLeft, Point3D bottomLeft, Point3D topRight, Point3D bottomRight) {
		updateExtents(topLeft);
		updateExtents(bottomRight);
		updateExtents(topRight);
		updateExtents(bottomRight);
	}
			
	/**
	 * Generates a fractal terrain based on the specified height, roughness constant and maximum no. of iterations. 
	 * Terrain features are generated by applying the random mid-point displacement procedures to 
	 * a rectangular ground plane. 
	 * <p>
	 * We begin by calculating the mid point of the ground plane. Elevation at this point is 
	 * calculated as the average elevation of all four corner vertices plus a random offset. 
	 * This is called a diamond step. 
	 * <p>
	 * Then we divide the ground plane at the mid-point of each edge to obtain four new grid positions. 
	 * Elevations at these mid-positions are calculated as the average elevation of the nearest two vertices 
	 * and the mid-point of the ground plane plus a random offset. 
	 * This is called a square step.
	 * <p>
	 * This procedure is repeated for each of the four new grid sections at each step for the specified no. of iterations
	 * Triangular surface patches for the terrain surface can be formed after the final grid is generated.
	 * <p>
	 * Random values can be obtained from a Gaussian distribution with mean 0 and variance proportional 
	 * to the peakHeight 
	 * 
	 * @param peakHeight height of the tallest point of the terrain
	 * @param roughnessConstant constant specifying how rough the terrain should be
	 * @param nIterations no. of iterations of the random mid-point displacement algorithm to perform
	 */
	public void generate(float peakHeight, float roughnessConstant, int nIterations) {
		float currentHeight = peakHeight;
				
		for (int iteration = 0; iteration < nIterations; iteration++) {
			int rows = (vertexGrid.getRows() - 1) * 2;
			int columns = (vertexGrid.getColumns() - 1) * 2;
//			System.out.println("Rows = " + rows + " Columns = " + columns);
			
			for (int rowIndex = 0; rowIndex < rows; rowIndex += 2) {
				vertexGrid.insertRow(rowIndex+1);
				for (int colIndex = 0; colIndex < columns; colIndex += 2) {
					int topRightColIndex = colIndex + 1;
					if (rowIndex != 0) topRightColIndex++;
					
					Rectangle3D cell = new Rectangle3D((Point3D)vertexGrid.getNode(rowIndex, colIndex), 
														(Point3D)vertexGrid.getNode(rowIndex + 2, colIndex),
														(Point3D)vertexGrid.getNode(rowIndex, topRightColIndex),
														(Point3D)vertexGrid.getNode(rowIndex + 2, colIndex+1));
					
					//Calculate mid-points
					float standardDeviation = currentHeight / 2.0f;
					Point3D midPoint5 = performDimondStep(cell.topLeft, cell.bottomLeft, cell.bottomRight, cell.topRight, standardDeviation);
					Point3D midPoint1 = performSquareStep(cell.topLeft, cell.bottomLeft, midPoint5, standardDeviation);
					Point3D midPoint3 = performSquareStep(cell.bottomRight, cell.topRight, midPoint5, standardDeviation);
					Point3D midPoint4 = performSquareStep(cell.topRight, cell.topLeft, midPoint5, standardDeviation);
					Point3D midPoint2 = performSquareStep(cell.bottomLeft, cell.bottomRight, midPoint5, standardDeviation);
					
					if (colIndex == 0) {
						vertexGrid.insertNode(rowIndex+1, colIndex, midPoint1);
						if (midPoint1.y < minElevation) minElevation = midPoint1.y;
					}					
					vertexGrid.insertNode(rowIndex+1, colIndex+1, midPoint5);
					if (midPoint5.y < minElevation) minElevation = midPoint5.y;
					
					vertexGrid.insertNode(rowIndex+1, colIndex+2, midPoint3);
					if (midPoint3.y < minElevation) minElevation = midPoint3.y;
					
					if (rowIndex == 0) {		
						vertexGrid.insertNode(rowIndex,   colIndex+1, midPoint4);
						if (midPoint4.y < minElevation) minElevation = midPoint4.y;
					}
					vertexGrid.insertNode(rowIndex+2, colIndex+1, midPoint2);
					if (midPoint2.y < minElevation) minElevation = midPoint2.y;
				}							
			}
			//Update height for next iteration			
			currentHeight = (currentHeight * (float)Math.pow(2, -roughnessConstant)) / 2.0f;
			fixPointsOnBoundary(minElevation);
		}
	}
	
	/**
	 * Performs diamond step in the random mid-point displacement algorithm. This involves calculating the 
	 * mid point of the ground plane. Elevation at this point is calculated as the average elevation of all 
	 * four corner vertices plus a random offset.
	 * @param a first corner point
	 * @param b second corner point
	 * @param c third corner point
	 * @param d fourth corner point
	 * @param standardDeviation allowed standard deviation 
	 * @return newly calculated mid-point of the ground plane
	 */
	private Point3D performDimondStep(Point3D a, Point3D b, Point3D c, Point3D d, float standardDeviation) {
		//Get a random no to calculate elevation
		float r = getRandomElevation(standardDeviation);		
		return new Point3D( (a.x + d.x)/2.0f, (a.y + b.y + c.y + d.y)/4.0f + r, (a.z + b.z)/2.0f );
	}
	
	/**
	 * Performs square step in the random mid-point displacement algorithm. This involves dividing the 
	 * ground plane at the mid-point of each edge to obtain four new grid positions. Elevations at these 
	 * mid-positions are calculated as the average elevation of the nearest two vertices and the mid-point 
	 * of the ground plane plus a random offset. 
	 * @param a first point of the edge
	 * @param b second point of the edge
	 * @param m mid-point of the ground plane
	 * @param standardDeviation allowed standard deviation 
	 * @return newly calculated mid point of the edge
	 */
	private Point3D performSquareStep(Point3D a, Point3D b, Point3D m, float standardDeviation) {
		float r = getRandomElevation(standardDeviation);		
		return new Point3D( (a.x + b.x)/2.0f, (a.y + b.y + m.y + m.y)/4.0f + r, (a.z + b.z)/2.0f);
	}
	
	/**
	 * Sets elevation of all the points on the boundary of the terrain to the specified elevation.
	 * @param elevation the height of the boundary points
	 */
	private void fixPointsOnBoundary(float elevation) {
		for (int rowIndex = 0; rowIndex < vertexGrid.getRows(); rowIndex++) {			
			for (int colIndex = 0; colIndex < vertexGrid.getColumns(); colIndex++) {
				
				Point3D p = (Point3D)vertexGrid.getNode(rowIndex, colIndex);
				
				if (p.x == minExtent.x)
					p.y = elevation;
				else if (p.x == maxExtent.x)
					p.y = elevation;
				else if (p.z == minExtent.z)
					p.y = elevation;
				else if (p.z == maxExtent.z)
					p.y = elevation;
			}
		}		
	}
		
	/**
	 * Configures the terrain geometry by calculating normals
	 */
	@Override
	public void configure() {				
		//Calculate normals
		calculateNormals();
	}
		
	/**
	 * Calculates vertex normals by calculating face normals and averaging them considering the faces shared by a vertex.
	 */
	private void calculateNormals() {
		//Calculate normal per triangle and add it to the stored normal value of the vertices
		normalGrid.addRows(1, 0);
		vertexOccurance.addRows(1, 0);
		
		for (int rowIndex = 0; rowIndex < vertexGrid.getRows()-1; rowIndex++) {
			normalGrid.addRows(1, 0);
			vertexOccurance.addRows(1, 0);
			
			for (int colIndex = 0; colIndex < vertexGrid.getColumns()-1; colIndex++) {
				Triangle triangle1 = new Triangle((Point3D)vertexGrid.getNode(rowIndex, 	  colIndex),
												   (Point3D)vertexGrid.getNode(rowIndex+1, colIndex),
												   (Point3D)vertexGrid.getNode(rowIndex,   colIndex+1));				
				Vector3D faceNormal1 = triangle1.faceNormal(false);
				
				Triangle triangle2 = new Triangle((Point3D)vertexGrid.getNode(rowIndex+1,  colIndex),
						  (Point3D)vertexGrid.getNode(rowIndex+1, colIndex+1),
						  (Point3D)vertexGrid.getNode(rowIndex,   colIndex+1));
				Vector3D faceNormal2 = triangle2.faceNormal(false);
				
				addFaceNormal(rowIndex, colIndex, faceNormal1);
				addFaceNormal(rowIndex+1, colIndex, faceNormal1);
				addFaceNormal(rowIndex, colIndex+1, faceNormal1);				
				addFaceNormal(rowIndex+1, colIndex+1, faceNormal2);
				addFaceNormal(rowIndex+1, colIndex, faceNormal2);
				addFaceNormal(rowIndex, colIndex+1, faceNormal2);
			}
		}
		
		//Calculate the average of the added normals
		for (int rowIndex = 0; rowIndex < vertexGrid.getRows(); rowIndex++) {			
			for (int colIndex = 0; colIndex < vertexGrid.getColumns(); colIndex++) {				
				Vector3D avgNormal = ((Vector3D)normalGrid.getNode(rowIndex, colIndex)).average((Integer)vertexOccurance.getNode(rowIndex, colIndex));
				normalGrid.setNode(rowIndex, colIndex, avgNormal);
			}
		}		
	}
	
	/**
	 * Adds face normal to the normalGrid which is then used to calculate vertex normals
	 * @param rowIndex index of row of the normal in the grid 
	 * @param colIndex index of the column of the normal in the grid
	 * @param faceNormal normal of the face to be stored
	 */
	private void addFaceNormal(int rowIndex, int colIndex, Vector3D faceNormal){
		Vector3D normal = (Vector3D)normalGrid.getNode(rowIndex, colIndex);
		if (normal == null) {
			normalGrid.insertNode(rowIndex, colIndex, faceNormal);
			vertexOccurance.insertNode(rowIndex, colIndex, new Integer(1));
		}
		else {
			normalGrid.setNode(rowIndex, colIndex, normal.add(faceNormal));
			vertexOccurance.setNode(rowIndex, colIndex, (Integer)vertexOccurance.getNode(rowIndex, colIndex)+1);
		}
	}
	
	/**
	 * Displays the fractal terrain using a display list. Based on the view settings, the terrain is displayed 
	 * either as solid or as a wireframe and with smooth or flat shading. A textured terrain is displayed if a texture 
	 * is applied to the terrain.   
	 */
	@Override
	public void display(GLAutoDrawable drawable) {
		Point3D point;
		Vector3D normal;
		
		GL gl = drawable.getGL();
				
		gl.glPolygonMode(GL.GL_FRONT_AND_BACK, ViewSettings.fillModel);
		gl.glShadeModel(ViewSettings.shadeModel);
	
		if( displayListID == -1 ) {			
			boolean	isTextureApplied = shouldApplyTexture() && texture != null;
			if (isTextureApplied) {
				texture.create(drawable);
			}
																		
			displayListID = gl.glGenLists(1);
			gl.glNewList(displayListID, GL.GL_COMPILE_AND_EXECUTE);
			
			if (isTextureApplied) {
				texture.load(gl);
				gl.glEnable(GL.GL_TEXTURE_2D);				
				gl.glTexEnvf(GL.GL_TEXTURE_ENV, GL.GL_TEXTURE_ENV_MODE, GL.GL_MODULATE);
			}
			else {
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			
			//Enable back face culling
			gl.glEnable(GL.GL_CULL_FACE);
	    	gl.glCullFace(GL.GL_BACK);
	    	
			//Apply material
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_AMBIENT, ambient, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_DIFFUSE, diffuse, 0);
			gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL.GL_SPECULAR, specular, 0);
			gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL.GL_SHININESS, shininess);
						
			for (int rowIndex = 0; rowIndex < vertexGrid.getRows()-1; rowIndex++) {
				gl.glBegin(GL.GL_TRIANGLE_STRIP);
				
				int colIndex = 0;
				if (isTextureApplied) {
					Tuple2f texCoord = (Tuple2f)textureCoordinates.getNode(rowIndex, colIndex);
					gl.glTexCoord2d(texCoord.a,texCoord.b);
				}
				normal = (Vector3D)normalGrid.getNode(rowIndex, colIndex);
				gl.glNormal3d(normal.x, normal.y, normal.z);			
				point = (Point3D)vertexGrid.getNode(rowIndex, colIndex);	
				gl.glVertex3d(point.x, point.y, point.z);
				
				if (isTextureApplied) {
					Tuple2f texCoord = (Tuple2f)textureCoordinates.getNode(rowIndex+1, colIndex);
					gl.glTexCoord2d(texCoord.a,texCoord.b);
				}
				normal = (Vector3D)normalGrid.getNode(rowIndex+1, colIndex);
				gl.glNormal3d(normal.x, normal.y, normal.z);
				point = (Point3D)vertexGrid.getNode(rowIndex+1, colIndex++);
				gl.glVertex3d(point.x, point.y, point.z);
				
				for (; colIndex < vertexGrid.getRowSize(rowIndex); ) {
					if (isTextureApplied) {
						Tuple2f texCoord = (Tuple2f)textureCoordinates.getNode(rowIndex, colIndex);
						gl.glTexCoord2d(texCoord.a,texCoord.b);
					}
					normal = (Vector3D)normalGrid.getNode(rowIndex, colIndex);
					gl.glNormal3d(normal.x, normal.y, normal.z);
					point = (Point3D)vertexGrid.getNode(rowIndex, colIndex);
					gl.glVertex3d(point.x, point.y, point.z);
					
					if (isTextureApplied) {
						Tuple2f texCoord = (Tuple2f)textureCoordinates.getNode(rowIndex+1, colIndex);
						gl.glTexCoord2d(texCoord.a,texCoord.b);
					}
					normal = (Vector3D)normalGrid.getNode(rowIndex+1, colIndex);
					gl.glNormal3d(normal.x, normal.y, normal.z);
					point = (Point3D)vertexGrid.getNode(rowIndex+1, colIndex++);
					gl.glVertex3d(point.x, point.y, point.z);
				}
				
				gl.glEnd();
			}
			
			if (isTextureApplied) {
				gl.glDisable(GL.GL_TEXTURE_2D);
			}
			
			gl.glDisable(GL.GL_CULL_FACE);
			gl.glEndList();
		}
		else
			gl.glCallList(displayListID);	
	}
	
	/**
	 * Loads texture from the specified file. Also generates texture coordinates at each grid point.
	 * @param textureFile path of the file holding the terrain texture
	 */
	public void setTexture(String textureFile) {		
		//Load texture from file
		System.out.print("Loading texture...");
		TextureLoader texLoader = new TextureLoader();
		setTexture(texLoader.getTexture(textureFile, true));
		System.out.println("done!");

		//Generate texture coordinates
		int rows = vertexGrid.getRows();
		int cols = vertexGrid.getColumns();
		float rowStep = 1.0f/(rows-1);
		float colStep = 1.0f/(cols-1);
		for (int rowIndex = 0; rowIndex < rows; rowIndex++) {
			textureCoordinates.addRows(1, 0);
			for (int colIndex = 0; colIndex < cols; colIndex++) {
				Tuple2f texCord = new Tuple2f(rowIndex*rowStep, colIndex*colStep);
				textureCoordinates.insertNode(rowIndex, colIndex, texCord);
			}
		}				
	}
	
	/**
	 * Specifies if texture should be applied to the terrain or not.
	 * @return true if the texture should be applied else false
	 */
	public boolean shouldApplyTexture() {
		return textureCoordinates.getRows() != 0;
	}
	
	/**
	 * Returns no. of vertex rows in the grid 
	 * @return no. of rows
	 */
	public int getRows() {
		return vertexGrid.getRows() - 1;
	}
	
	/**
	 * Returns no. of vertex columns in the grid
	 * @return no. of columns
	 */
	public int getColumns() {
		return vertexGrid.getColumns() - 1;
	}
	
	/**
	 * Returns width of a grid cell  
	 * @return cell width
	 */
	public int getCellWidth() {
		return (int)( (maxExtent.x - minExtent.x) / getColumns());
	}
	
	/**
	 * Returns depth of a grid cell
	 * @return cell depth
	 */
	public int getCellDepth() {
		return (int)( (maxExtent.z - minExtent.z) / getRows());
	}
	
	/**
	 * Returns 3D co-ordinate of a vertex in the grid 
	 * @param rowIndex index of row where the vertex is present
	 * @param colIndex index of column where the vertex is present
	 * @return 3D co-ordinate of the vertex
	 */
	public Point3D getPoint(int rowIndex, int colIndex) {
		return (Point3D)vertexGrid.getNode(rowIndex, colIndex);
	}
	
	/**
	 * Returns elevation of a point on the terrain which is at a lowest height  
	 * @return minimum elevation
	 */
	public float getMinElevation() {
		return minElevation;
	}
	
	/**
	 * Returns row and column index of a vertex in the grid whose 3D co-ordinates are specified by point
	 * @param point 3D co-ordinates of a vertex 
	 * @return cell index in the form (row index, column index)
	 */
	public Tuple2i getCellIndex(Point3D point) {
		Point3D topLeft = getPoint(0, 0);
		Tuple2i cellIndex = new Tuple2i(-1, -1);
		if (point.z > topLeft.z) cellIndex.a = (int)((point.z - topLeft.z) / getCellDepth());
		if (point.x > topLeft.x) cellIndex.b = (int)((point.x - topLeft.x) / getCellWidth()); 
		return cellIndex;
	}
	
	/**
	 * Returns elevation of a point on the terrain. This function computes intersection between a vertical ray 
	 * and terrain triangles to find out exact elevation at the given point.  
	 * @param xzPoint x and z coordinates of a point whose elevation needs to be returned 
	 * @return elevation
	 */
	public float getElevation(Point3D xzPoint) {
		//Update terrain grid cell index according to the x-z co-ordinates 
		Tuple2i newCellIndex = getCellIndex(xzPoint);
		if (newCellIndex.a < 0 || newCellIndex.a >= getRows() || newCellIndex.b < 0 || newCellIndex.b >= getColumns()) {
			System.out.println("!!!!Point is out of the terrain boundaries!!!!");			
			return -10000;
		}
				
		Triangle t = null;
		Point3D elevatedPosition;
		Ray ray = new Ray(new Point3D(xzPoint.x, -10000, xzPoint.z), new Vector3D(0, 1, 0));			
		Point3D cellTopLeft = getPoint(newCellIndex.a, newCellIndex.b);	

		//Upper triangle
		t = new Triangle(cellTopLeft, getPoint(newCellIndex.a+1, newCellIndex.b), getPoint(newCellIndex.a, newCellIndex.b+1));
		elevatedPosition = t.getIntersection(ray);
		if (elevatedPosition != null) {
			return elevatedPosition.y;			
		}
		
		//Lower traingle
		t = new Triangle(getPoint(newCellIndex.a+1, newCellIndex.b),getPoint(newCellIndex.a+1, newCellIndex.b+1), getPoint(newCellIndex.a, newCellIndex.b+1));
		elevatedPosition = t.getIntersection(ray);
		if (elevatedPosition != null) {
			return elevatedPosition.y;		
		}
		
		return -10000;
	}
	
	/**
	 * Saves this fractal terrain in binary format in a file
	 * @param filePath path of the file where terrain information is to be saved
	 * @throws IOException thrown in case of problem accessing and writing to the specified file
	 */
	public void save(String filePath) throws IOException {
		ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(filePath));
		try {
			oos.writeObject(this);	            
		} 
	    finally {
	        oos.close();
	    }
	}
	
	/**
	 * Loads a fractal terrain from the specified file. 
	 * @param filePath path of the file which contains terrain information in binary format
	 * @return terrain object created using information from the file
	 * @throws IOException thrown in case of problem accessing and reading the specified file
	 */
	public static FractalTerrain load(String filePath) throws IOException {
		FractalTerrain terrain = null;
		ObjectInputStream ois = new ObjectInputStream(new FileInputStream(filePath));
		try {
			terrain = (FractalTerrain)ois.readObject();
			terrain.displayListID = -1;
			if (terrain.texture != null) {
				String textureFile = terrain.texture.getResourceName();
				if (!textureFile.equals("")) {
					TextureLoader texLoader = new TextureLoader();
					terrain.texture = texLoader.getTexture(textureFile, true);
				}
			}
		} 
		catch (ClassNotFoundException cnf) {
			cnf.printStackTrace();
		}
	    finally {
	        ois.close();
	    }
	    return terrain;
	}
	
	/**
	 * Returns a random number between [-standardDeviation, +standardDeviation]
	 * @param standardDeviation range of the random no.
	 * @return random no.
	 */
	private float getRandomElevation(float standardDeviation) {		
		return (float)randomNoGenerator.nextGaussian() * standardDeviation;		
	}	
}
