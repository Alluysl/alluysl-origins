package alluysl.alluyslorigins.util;

public class OverlayInfo {

    public float ratio;
    public float interpolatedRatio;

    public OverlayInfo(boolean activeOnFirstTick){
        interpolatedRatio = ratio = activeOnFirstTick ? 1.0F : 0.0F;
    }
}
