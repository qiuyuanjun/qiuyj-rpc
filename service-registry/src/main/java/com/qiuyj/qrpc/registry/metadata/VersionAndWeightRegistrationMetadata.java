package com.qiuyj.qrpc.registry.metadata;

/**
 * @author qiuyj
 * @since 2018-10-06
 */
public class VersionAndWeightRegistrationMetadata extends RegistrationMetadata implements VersionAndWeightRegistration {

  private static final long serialVersionUID = 2881629977960680335L;

  /** 版本号 */
  private String version;

  /** 权重 */
  private int weight;

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public int getWeigth() {
    return weight;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }
}
