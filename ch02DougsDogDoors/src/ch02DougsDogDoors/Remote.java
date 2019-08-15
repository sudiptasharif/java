package ch02DougsDogDoors;

public class Remote {
	private DogDoor door;
	
	public Remote(DogDoor aDoor) {
		door = aDoor;
	}
	
	public void pressButton() {
		System.out.println("Pressing the remote control button...");
		if(door.isOpen()) {
			door.close();
		}else {
			door.open();
		}
	}
}
