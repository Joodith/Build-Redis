package com.connections.handlers.timeoutHandler;

public class LastActivityTimeMap {
    private final Object entity;
    private long activityTime;
    private long ttl=-1;
    public LastActivityTimeMap(Object thisEntity){
        entity=thisEntity;
        activityTime=System.currentTimeMillis();
    }

    public LastActivityTimeMap(Object thisEntity,long thisActivityTime){
        entity=thisEntity;
        activityTime=thisActivityTime;
    }
    public LastActivityTimeMap(Object thisEntity,long thisActivityTime,long thisTtl){
        entity=thisEntity;
        activityTime=thisActivityTime;
        ttl=thisTtl;
    }

    public long getTtl() {
        return ttl;
    }

    public void setTtl(long ttl) {
        this.ttl = ttl;
    }

    public Object getEntity() {
        return entity;
    }



    public long getActivityTime() {
        return activityTime;
    }

    public void setActivityTime() {
        this.activityTime =System.currentTimeMillis();
    }
}
