public interface Draggable<T> extends MouseInteractable {

    public interface CallBack {
        default void onStartDrag() { }
        default void onReject() {}
        default void onAccept() {}
    }
    
    public void startDrag();

    public void endDrag();

    public void update();

    public void addTarget(DragTarget<T> target);

    public void addCallback(CallBack callBack);

    public T getPayload();
}
