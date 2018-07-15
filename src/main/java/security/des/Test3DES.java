package security.des;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.*;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * 3DES
 * 密钥长度增强
 * 迭代次数提高
 */
public class Test3DES {

    private Key getSecretKey(){
        //生成key
        Key key = null;
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("DESede");
            keyGenerator.init(168);
            SecretKey secretKey = keyGenerator.generateKey();
            byte[] bytesKey = secretKey.getEncoded();
            //key转换
            DESedeKeySpec desKeySpec = new DESedeKeySpec(bytesKey);
            SecretKeyFactory factory = SecretKeyFactory.getInstance("DESede");
            key = factory.generateSecret(desKeySpec);
            System.out.println(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return key;
    }

    /**
     * 加密
     */
    public String encrypt(String content){
        Key secretKey = getSecretKey();
        String result = null;
        try {
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE,secretKey);
            byte[] bytes = cipher.doFinal(content.getBytes());
            result = Hex.encodeHexString(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 解密
     */
    public  String decrypt(String content){
        Key secretKey = getSecretKey();
        try {
            Cipher cipher = Cipher.getInstance("DESede/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE,secretKey);
            byte[] bytes = cipher.doFinal(content.getBytes());
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }




    public static void main(String[] args) {
        String content = "hello word";
        Test3DES test3DES = new Test3DES();
        Key secretKey = test3DES.getSecretKey();
        System.out.println(secretKey);
        String encryptContent = test3DES.encrypt(content);
        System.out.println("加密后:"+encryptContent);
        String decryptContent =  test3DES.decrypt(encryptContent);
        System.out.println("解密后:"+decryptContent);
    }
}
