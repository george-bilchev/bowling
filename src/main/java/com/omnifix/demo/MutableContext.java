package com.omnifix.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import reactor.util.context.Context;

class MutableContext implements Context {
  HashMap<Object, Object> holder = new HashMap<>();

  @SuppressWarnings("unchecked")
  @Override
  public <T> T get(Object key) {
    return (T) holder.get(key);
  }

  @Override
  public boolean hasKey(Object key) {
    return holder.containsKey(key);
  }

  @Override
  public Context put(Object key, Object value) {
    holder.put(key, value);
    return this;
  }

  @Override
  public Context delete(Object key) {
    holder.remove(key);
    return this;
  }

  @Override
  public Stream<Map.Entry<Object, Object>> stream() {
    return holder.entrySet().stream();
  }

  @Override
  public int size() {
    return holder.size();
  }
}
