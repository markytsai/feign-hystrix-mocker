package feign.hystrix;

/**
 * @description:
 * @author: caizhenya
 * @email: caizhenya@shandiantech.com
 * @date: 2021/3/22 17:25
 */
public class MockException extends RuntimeException {

  public MockException(String msg) {
    super(msg);
  }
}
