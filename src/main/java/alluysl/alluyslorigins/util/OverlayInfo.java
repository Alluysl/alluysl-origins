package alluysl.alluyslorigins.util;

public class OverlayInfo {

    public float ratio;

    public OverlayInfo(boolean activeOnFirstTick){
        ratio = activeOnFirstTick ? 1.0F : 0.0F;
    }
}
