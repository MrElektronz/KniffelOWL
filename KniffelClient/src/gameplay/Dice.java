package gameplay;

import java.util.ArrayList;
import java.util.Random;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.geometry.Point3D;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Dice extends MeshView{
	

	private static final Image[] sides = new Image[] {new Image(Dice.class.getResourceAsStream("dice1.png")),
			new Image(Dice.class.getResourceAsStream("dice2.png")),new Image(Dice.class.getResourceAsStream("dice3.png")),
			new Image(Dice.class.getResourceAsStream("dice4.png")),new Image(Dice.class.getResourceAsStream("dice5.png")),
			new Image(Dice.class.getResourceAsStream("dice6.png"))};
	
	private int number;
	public Dice() {

		randomTransforms(null);
		
		setMesh(createMesh(1,1,1));

		setScale(70);
	}
	
	public Dice(int number) {
		this.number = number;
		randomTransforms(null);
		
		setMesh(createMesh(1,1,1));

		setScale(70);
		setSide(number);
	}
	
	
	 static TriangleMesh createMesh(float w, float h, float d) {

	        if (w * h * d == 0) {
	            return null;
	        }

	        float hw = w / 2f;
	        float hh = h / 2f;
	        float hd = d / 2f;

	        float x0 = 0f;
	        float x1 = 1f / 4f;
	        float x2 = 2f / 4f;
	        float x3 = 3f / 4f;
	        float x4 = 1f;
	        float y0 = 0f;
	        float y1 = 1f / 3f;
	        float y2 = 2f / 3f;
	        float y3 = 1f;

	        TriangleMesh mesh = new TriangleMesh();
	        mesh.getPoints().addAll(
	                hw, hh, hd,   //point A
	                hw, hh, -hd,  //point B
	                hw, -hh, hd,  //point C
	                hw, -hh, -hd, //point D
	                -hw, hh, hd,  //point E
	                -hw, hh, -hd, //point F
	                -hw, -hh, hd, //point G
	                -hw, -hh, -hd //point H
	        );
	        mesh.getTexCoords().addAll(
	                x1, y0,
	                x2, y0,
	                x0, y1,
	                x1, y1,
	                x2, y1,
	                x3, y1,
	                x4, y1,
	                x0, y2,
	                x1, y2,
	                x2, y2,
	                x3, y2,
	                x4, y2,
	                x1, y3,
	                x2, y3
	        );
	        mesh.getFaces().addAll(
	                0, 10, 2, 5, 1, 9,   //triangle A-C-B
	                2, 5, 3, 4, 1, 9,    //triangle C-D-B
	                4, 7, 5, 8, 6, 2,    //triangle E-F-G
	                6, 2, 5, 8, 7, 3,    //triangle G-F-H
	                0, 13, 1, 9, 4, 12,  //triangle A-B-E
	                4, 12, 1, 9, 5, 8,   //triangle E-B-F
	                2, 1, 6, 0, 3, 4,    //triangle C-G-D
	                3, 4, 6, 0, 7, 3,    //triangle D-G-H
	                0, 10, 4, 11, 2, 5,  //triangle A-E-C
	                2, 5, 4, 11, 6, 6,   //triangle C-E-G
	                1, 9, 3, 4, 5, 8,    //triangle B-D-F
	                5, 8, 3, 4, 7, 3     //triangle F-D-H
	        );
	        mesh.getFaceSmoothingGroups().addAll(
	                0, 0, 1, 1, 2, 2, 3, 3, 4, 4, 5, 5
	        );
	        return mesh;
	    }
	
	
	public void randomTransforms(ArrayList<Dice> arr) {
		getTransforms().clear();
		setSide(new Random().nextInt(6)+1);

		
		Random r = new Random();
		if(arr == null) {
		translate(r.nextInt(450)+100, r.nextInt(450)+100, 0);
		rotate(r.nextInt(359),Rotate.Z_AXIS);
		}else {
			for(int i = 0; i<arr.size();i++) {
				int x = 0;
				int y = 0;
				do {
					x = r.nextInt(450)+100;
					y = r.nextInt(450)+100;
					translate(x, y, 0);
					rotate(r.nextInt(359),Rotate.Z_AXIS);
				}while(!canSpawn(x, y, arr));
			}
		}
		
	}
	
	
	
	public void setSide(int number) {
		this.number = number;
		PhongMaterial mat = new PhongMaterial(Color.WHITE);
		mat.setSpecularColor(Color.BLACK);
		System.out.println(number);
		mat.setDiffuseMap(sides[number-1]);
		setMaterial(mat);

	}
	
	public int getNumber() {
		return number;
	}
	
	/**
	 * doesnt make the dice spawn on each other
	 * @param x
	 * @param y
	 * @param arr
	 * @return
	 */
	private boolean canSpawn(int x, int y, ArrayList<Dice> arr) {
		x = Math.abs(x);
		y = Math.abs(y);
		for(int i = 0; i<arr.size();i++) {
			double posX = Math.abs(arr.get(i).getTranslateX());
			double posY = Math.abs(arr.get(i).getTranslateY());
			double distanceX = Math.abs(posX-x);
			double distanceY = Math.abs(posY-y);
			if(distanceX+distanceY <100  && arr.get(i) != this) {
				return false;
			}
		}
		return true;
	}
	
	public void setScale(double s) {
		setScaleX(s);
		setScaleY(s);
		setScaleZ(s);

	}
	
	public void translate(double x, double y, double z) {
		setTranslateX(x);
		setTranslateY(y);
		setTranslateZ(z);
		}
	

	
	private void rotate(double value, Point3D i) {
		Rotate rot = new Rotate(value, i);
		getTransforms().add(rot);
	}
	
	public void roll() {
		
		setScale(0);
		RollDiceThread rThread = new RollDiceThread(90);
		rThread.start();
       
		/*RotateTransition rotate = new RotateTransition();
        rotate.setAxis(Rotate.X_AXIS);
        rotate.setByAngle(360*2);
        rotate.setCycleCount(1);
        rotate.setDuration(Duration.millis(1000));
        rotate.setNode(this);
        rotate.play();*/
	}
	
	
	private class RollDiceThread extends Thread{
		
		private long counter,rollAmount;
		private Rotate rot;
		/**
		 * 
		 * @param rollAmount should be a number, so that: 4*rollAmount % 360 = 0 (rollAmount % 90 = 0) (So that the starting face will be shown)
		 */
		public RollDiceThread(long rollAmount) {
			this.rollAmount = rollAmount;
			this.counter = 0;
			rot = new Rotate(4,Rotate.X_AXIS);
			
		}
		
		
		@Override
		public void run() {
			while(counter < rollAmount) {
			counter++;
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					setScale((counter*70/rollAmount));
					getTransforms().add(rot);
				}
				
			});
			//getTransforms().add(trans);
			try {
				sleep(5);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
			
		}
		
	}

}
