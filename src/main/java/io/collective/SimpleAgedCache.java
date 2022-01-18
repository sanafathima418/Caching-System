package io.collective;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

public class SimpleAgedCache {
    Clock clock;
    CacheEntry[] cache = new CacheEntry[10];
    int idx = 0;

    public SimpleAgedCache(Clock clock) {
        this.clock = clock;
    }

    public SimpleAgedCache() {
        this.clock = new Clock() {
            @Override
            public ZoneId getZone() {
                return systemDefaultZone().getZone();
            }

            @Override
            public Clock withZone(ZoneId zone) {
                return Clock.system(zone);
            }

            @Override
            public Instant instant() {
                return systemDefaultZone().instant();
            }
        };
    }

    public void put(Object key, Object value, int retentionInMillis) {
         Clock curr_time = new Clock() {
             @Override
             public ZoneId getZone() {
                 return systemDefaultZone().getZone();
             }

             @Override
             public Clock withZone(ZoneId zone) {
                 return Clock.system(zone);
             }

             @Override
             public Instant instant() {
                 return systemDefaultZone().instant();
             }
         };
         this.cache[idx++] = new CacheEntry(key,value,retentionInMillis,curr_time);
    }

    public boolean isEmpty() {
        if (this.idx == 0) {
            return true;
        }
        return false;
    }

    public void swap(int swapIdx){
        for(int i = this.idx-1;i>-1;i--) {
            if ((this.clock.millis() - this.cache[i].insert_time.millis()) >= this.cache[i].retentionInMillis) {
                --this.idx;
            }
            else {
                break;
            }
        }
        this.cache[swapIdx] = this.cache[this.idx-1];
        --this.idx;

    }

    public void checkExpired(){
        for(int i=0;i<=this.cache.length;i++){
            if(this.cache[i] == null){
                return;
            }
            if(i<this.idx) {
                if ((this.clock.millis() - this.cache[i].insert_time.millis()) >= this.cache[i].retentionInMillis) {
                    swap(i);
                }
            }
        }
    }

    public int size() {
        checkExpired();
        return this.idx;
    }

    public Object get(Object key) {
        checkExpired();
        if (isEmpty()) {
            return null;
        }
        for(int i=0;i<this.idx;i++){
            if (this.cache[i].key == key){
                return this.cache[i].value;
            }
        }
        return null;
    }
}

class CacheEntry {
    Object key;
    Object value;
    Clock insert_time;
    int retentionInMillis;

    public CacheEntry(Object key,Object value,int retentionInMillis, Clock curr_time){
        this.key = key;
        this.value = value;
        this.insert_time = curr_time;
        this.retentionInMillis = retentionInMillis;
    }
}