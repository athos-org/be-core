package org.athos.core.service.secure;

/**
 * Service for interacting with a secure key-value store.
 */
public interface SecureStoreService {

  /**
   * Retrieves the value associated with the given key from the secure store.
   *
   * @param key the key whose associated value is to be returned
   * @return the value associated with the specified key
   */
  String get(String key);

  /**
   * Stores the given key-value pair in the secure store.
   *
   * @param key   the key with which the specified value is to be associated
   * @param value the value to be associated with the specified key
   */
  void set(String key, String value);

}
