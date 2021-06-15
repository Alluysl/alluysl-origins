package alluysl.alluyslorigins.util;

public class OverlayInfo {

    private float ratio;
    private float previousRatio;

    public OverlayInfo(boolean activeOnFirstTick){
        previousRatio = ratio = activeOnFirstTick ? 1.0F : 0.0F;
    }

    public float getRatio() {
        return ratio;
    }

    public float getPreviousRatio() {
        return previousRatio;
    }

    public void setRatio(float newRatio, boolean updatePrevious) {
        if (updatePrevious)
            previousRatio = ratio;
        ratio = newRatio;
    }
}
