/*
 *  Copyright 2015 University of Ulm
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package de.uniulm.omi.cloudiator.util;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import org.apache.commons.codec.binary.Base64;

/**
 * Utility class for passwords.
 *
 * @author Daniel Baur
 */
public class Password {

  /**
   * Iterations for pbkdef2
   */
  private static final int ITERATIONS = 10 * 1024;
  /**
   * length of the generated salt
   */
  private static final int SALT_LENGTH = 32;
  /**
   * key length for pbkdef2
   */
  private static final int DESIRED_KEY_LENGTH = 512;
  /**
   * Instance of the class (singleton)
   */
  private static Password instance = null;
  /**
   * Random generator.
   */
  private final SecureRandom random;

  /**
   * Constructor.
   */
  private Password() {
    try {
      this.random = SecureRandom.getInstance("SHA1PRNG", "SUN");
    } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
      throw new IllegalStateException(e);
    }
  }

  /**
   * Returns the instance of the password class. <p> Implementation of singleton pattern.
   *
   * @return the instance of the password class
   */
  public static Password getInstance() {
    if (instance == null) {
      instance = new Password();
    }
    return instance;
  }

  /**
   * Generates a salt.
   *
   * @return the generated salt.
   */
  public byte[] generateSalt() {
    byte[] salt = new byte[SALT_LENGTH];
    random.nextBytes(salt);
    return salt;
  }

  /**
   * Generates a token.
   *
   * @return the generated token.
   */
  public String generateToken() {
    return new BigInteger(1024, random).toString(32);
  }

  /**
   * Hashes the password using pbkdf2 and the given salt.
   *
   * @param password the plain text password to hash.
   * @param salt the salt for the password
   * @return the hashed password
   */
  public char[] hash(char[] password, byte[] salt) {
    checkNotNull(password);
    checkArgument(password.length != 0);
    checkNotNull(salt);
    checkArgument(salt.length != 0);

    try {
      SecretKeyFactory f = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
      SecretKey key =
          f.generateSecret(new PBEKeySpec(password, salt, ITERATIONS, DESIRED_KEY_LENGTH));
      return Base64.encodeBase64String(key.getEncoded()).toCharArray();
    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
      throw new IllegalStateException("Could not hash the password of the user.", e);
    }
  }

  /**
   * Checks if the given plain password matches the given password hash
   *
   * @param plain the plain text password.
   * @param database the hashed password from the database.
   * @param salt the salt used for the hashed password.
   * @return true if the passwords match, false if not.
   */
  public boolean check(char[] plain, char[] database, byte[] salt) {

    checkNotNull(plain);
    checkArgument(plain.length != 0, "Plain must not be empty.");
    checkNotNull(database);
    checkArgument(database.length != 0, "Database must not be empty.");
    checkNotNull(salt);
    checkArgument(salt.length != 0, "Salt must not be empty.");

    char[] hashedPlain = this.hash(plain, salt);
    return Arrays.equals(hashedPlain, database);
  }

}
