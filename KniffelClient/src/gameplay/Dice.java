package gameplay;

import java.util.Random;

import javafx.animation.RotateTransition;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Transform;
import javafx.scene.transform.Translate;
import javafx.util.Duration;

public class Dice extends Box{
	
	
	
	public Dice() {
		setWidth(70);
		setHeight(70);
		setDepth(70);
		PhongMaterial mat = new PhongMaterial(Color.WHITE);
		mat.setSpecularColor(Color.BLACK);
		setMaterial(mat);
		randomTransforms();
	}
	
	public void randomTransforms() {
		Random r = new Random();
		translate(r.nextInt(450)+100, r.nextInt(450)+100, 0);
		rotate(r.nextInt(359));
		
	}
	
	public void setScale(double s) {
		setWidth(s);
		setHeight(s);
		setDepth(s);
	}
	
	public void translate(double x, double y, double z) {
		setTranslateX(x);
		setTranslateY(y);
		setTranslateZ(z);
	}
	

	
	private void rotate(double value) {
		Rotate rot = new Rotate(value, Rotate.Z_AXIS);
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
		private Translate trans;
		/**
		 * 
		 * @param rollAmount should be a number, so that: 4*rollAmount % 360 = 0 (rollAmount % 90 = 0) (So that the starting face will be shown)
		 */
		public RollDiceThread(long rollAmount) {
			this.rollAmount = rollAmount;
			this.counter = 0;
			rot = new Rotate(4,Rotate.X_AXIS);
			trans = new Translate(-1,0,0);
		}
		
		
		@Override
		public void run() {
			while(counter < rollAmount) {
			counter++;
			Platform.runLater(new Runnable() {

				@Override
				public void run() {
					setScale((counter*70/rollAmount));
					System.out.println(getWidth());
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
