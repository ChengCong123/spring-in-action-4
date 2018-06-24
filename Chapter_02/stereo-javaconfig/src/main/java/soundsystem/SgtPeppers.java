package soundsystem;


public class SgtPeppers implements CompactDisc {

  private String title = "Sgt. Pepper's Lonely Hearts Club Band";  
  private String artist = "The Beatles\n";
  
  public void play() {
    System.out.print("Playing " + title + " by " + artist);
  }

}
