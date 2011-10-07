import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.RenderingHints;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

import org.rsbot.event.events.MessageEvent;
import org.rsbot.event.listeners.MessageListener;
import org.rsbot.event.listeners.PaintListener;
import org.rsbot.script.Script;
import org.rsbot.script.ScriptManifest;
import org.rsbot.script.methods.MethodContext;
import org.rsbot.script.methods.Skills;
import org.rsbot.script.util.Timer;
import org.rsbot.script.wrappers.RSObject;

@ScriptManifest(authors = { "Swipe" , "Joshht"}, keywords = "Combat, loot", name = "ActiveCannon", version = 1.0, description = "Cannon Loader")
public class ActiveCannon extends Script implements MessageListener, PaintListener{
boolean reload=true;
boolean decay;
int reloads;
private long startTime;
Tracker t;
Image i1,i2;
boolean setUp;
public boolean onStart(){
	startTime = System.currentTimeMillis();
	t= new Tracker();
	i1= getImage("http://i.imgur.com/0kLe7.png");
	i2 = getImage("http://i.imgur.com/k8ce3.png");
	return true;
}
	@Override
	public int loop() {
		RSObject Cannon = objects.getNearest(6);
		if(interfaces.canContinue()){
			interfaces.clickContinue();
			sleep(1000);
		}
		if(inventory.contains(6)){
			setUp=true;
		}
		if(!inventory.contains("cannonball")){
			log("No more cannonballs");
			stopScript();
			
		}
		if(setUp){
			if(inventory.contains(6)){
			inventory.getItem(6).interact("set-up");
			//Poor way to wait, but oh well
			sleep(15000);
			reload=true;
			} else{
				setUp=false;
			}
		}
		if(reload){
			if(Cannon!=null){
				//Could try to make reloading with failsafes... this works :)
			Cannon.doClick();
			sleep(5500);
			Cannon.doClick();
			reload=false;
			setUp=false;
		reloads++;	
			}
		}
		if(decay){
			if(Cannon!=null){
			Cannon.interact("pick-up");
			sleep(15000);
		decay=false;
		setUp=true;
			reload=true;
		}
		}
		return 300;
	}

	@Override
	public void messageReceived(MessageEvent m) {
		//If players say these, maybe it will trigger it?
		if(m.getMessage().contains("cannon is out of ammo")){
			reload=true;
		}
		if(m.getMessage().contains("decay")){
			decay=true;
		}
		
	}
	private void drawMouse(Graphics g1) {
		((Graphics2D) g1).setRenderingHints(new RenderingHints(
				RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON));
		Point p = mouse.getLocation();
		Graphics2D spinG = (Graphics2D) g1.create();
		Graphics2D spinGRev = (Graphics2D) g1.create();
		spinG.setColor(Color.red);
		spinGRev.setColor(Color.white);
		spinG.rotate(System.currentTimeMillis() % 2000d / 2000d * (360d) * 2
				* Math.PI / 180.0, p.x, p.y);
		spinGRev.rotate(System.currentTimeMillis() % 2000d / 2000d * (-360d)
				* 2 * Math.PI / 180.0, p.x, p.y);
		final int outerSize = 20;
		final int innerSize = 15;
		spinG.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		spinGRev.setStroke(new BasicStroke(4.0f, BasicStroke.CAP_ROUND,
				BasicStroke.JOIN_ROUND));
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize,
				outerSize, 100, 75);
		spinG.drawArc(p.x - (outerSize / 2), p.y - (outerSize / 2), outerSize,
				outerSize, -100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2),
				innerSize, innerSize, 100, 75);
		spinGRev.drawArc(p.x - (innerSize / 2), p.y - (innerSize / 2),
				innerSize, innerSize, -100, 75);
	}
	@Override
	public void onRepaint(Graphics g) {
		g.drawImage(i1,75,20,null);
		g.drawImage(i2,60,380,null);
		g.setColor(Color.DARK_GRAY);
		g.fill3DRect(95, 440,250,40,true);
		g.setColor(Color.white);
		g.drawString("Reloads: "+reloads + " Total CannonBalls: "+ (reloads*30) , 105,460);
		g.drawString("Time: "+Timer.format(System.currentTimeMillis()-startTime)+" | Exp: "+t.addAll() + "| Exp/H: "+t.allPerHour(), 105, 475);
		drawMouse(g);
		t.updateSkills();
	}
	public int PerHour(int i) {
		return (int) Math.ceil(i * 3600000D
				/ Math.abs(startTime - System.currentTimeMillis()));
	}

	private Image getImage(String url) {
		try {
			return ImageIO.read(new URL(url));
		} catch (IOException e) {
			return null;
		}
	}
	public class Tracker {
		long start;
		public int length = 40;
		int bSkills[] = new int[7];// attack,str,def,range,mage,hp,prayer
		int cSkills[] = new int[7];// attack,str,def,range,mage,hp,prayer
		MethodContext m;
		String skillNames[] = { "Attack", "Strength", "Defense", "Range",
				"Magic", "Consitution", "Prayer" };

		/**
		 * @param methodGather
		 *            Obtains all information to be tracked
		 */
		public Tracker() {
			start = System.currentTimeMillis();
			m = ctx;
			bSkills[0] = m.skills.getCurrentExp(Skills.ATTACK);
			bSkills[1] = m.skills.getCurrentExp(Skills.STRENGTH);
			bSkills[2] = m.skills.getCurrentExp(Skills.DEFENSE);
			bSkills[3] = m.skills.getCurrentExp(Skills.RANGE);
			bSkills[4] = m.skills.getCurrentExp(Skills.MAGIC);
			bSkills[5] = m.skills.getCurrentExp(Skills.CONSTITUTION);
			bSkills[6] = m.skills.getCurrentExp(Skills.PRAYER);
		}

		/**
		 * Updates skill exp
		 */
		void updateSkills() {
			cSkills[0] = m.skills.getCurrentExp(Skills.ATTACK);
			cSkills[1] = m.skills.getCurrentExp(Skills.STRENGTH);
			cSkills[2] = m.skills.getCurrentExp(Skills.DEFENSE);
			cSkills[3] = m.skills.getCurrentExp(Skills.RANGE);
			cSkills[4] = m.skills.getCurrentExp(Skills.MAGIC);
			cSkills[5] = m.skills.getCurrentExp(Skills.CONSTITUTION);
			cSkills[6] = m.skills.getCurrentExp(Skills.PRAYER);
		}

		/**
		 * Draws all possible xp gains
		 * 
		 * @param g
		 * @return g
		 */
		public Graphics drawAnyIfChanged(Graphics g, int xl, int yl) {
			int a = 1;
			for (int i = 0; i < skillNames.length; i++) {
				if (skillChanged(i)) {
					a++;
					g.setColor(Color.WHITE);
					g.drawString(skillNames[i] + " exp gained: "
							+ (cSkills[i] - bSkills[i]), xl, yl + (a * 30));
					g.drawString(skillNames[i] + " exp/h: "
							+ (PerHour(cSkills[i] - bSkills[i])), xl, yl
							+ (a * 30) + 15);
				}

			}
			length = (a) * 35;
			return g;
		}

		public int addAll() {
			int all = 0;
			for (int i = 0; i < skillNames.length; i++) {
				if (skillChanged(i)) {
					all += cSkills[i] - bSkills[i];
				}
			}
			return all;

		}

		public int allPerHour() {
			return PerHour(addAll());
		}

		boolean skillChanged(int skill) {
			return cSkills[skill] > bSkills[skill];
		}

		/**
		 * 
		 * @return if skills have been changed
		 */
		boolean skillsChanged() {
			updateSkills();
			for (int i : bSkills) {
				for (int j : cSkills) {
					if (j > i) {
						return true;
					}
				}
			}
			return false;

		}
	}
}
