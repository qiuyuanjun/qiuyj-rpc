package com.qiuyj.qrpc.registry.metadata;

import java.util.Objects;

/**
 * @author qiuyj
 * @since 2018-10-06
 */
public class VersionAndWeightRegistrationMetadata extends RegistrationMetadata implements VersionAndWeightRegistration {

  private static final long serialVersionUID = 5699709875383937737L;

  /** 版本号 */
  private String version;

  /** 权重 */
  private int weight;

  @Override
  public String getVersion() {
    return version;
  }

  @Override
  public int getWeight() {
    return weight;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof VersionAndWeightRegistrationMetadata)) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }
    VersionAndWeightRegistrationMetadata that = (VersionAndWeightRegistrationMetadata) o;
    return weight == that.weight &&
        Objects.equals(version, that.version);
  }

  @Override
  public int hashCode() {
    return Objects.hash(super.hashCode(), version, weight);
  }
}
