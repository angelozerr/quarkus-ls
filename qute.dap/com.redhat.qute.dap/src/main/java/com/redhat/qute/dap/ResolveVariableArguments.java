package com.redhat.qute.dap;

import org.eclipse.xtext.xbase.lib.Pure;
import org.eclipse.xtext.xbase.lib.util.ToStringBuilder;

/**
 * Arguments for 'scopes' request.
 */
@SuppressWarnings("all")
public class ResolveVariableArguments {
  /**
   * Retrieve the scopes for this stackframe.
   */
  private int frameId;
  
  /**
   * Retrieve the scopes for this stackframe.
   */
  @Pure
  public int getFrameId() {
    return this.frameId;
  }
  
  /**
   * Retrieve the scopes for this stackframe.
   */
  public void setFrameId(final int frameId) {
    this.frameId = frameId;
  }
  
  @Override
  @Pure
  public String toString() {
    ToStringBuilder b = new ToStringBuilder(this);
    b.add("frameId", this.frameId);
    return b.toString();
  }
  
  @Override
  @Pure
  public boolean equals(final Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ResolveVariableArguments other = (ResolveVariableArguments) obj;
    if (other.frameId != this.frameId)
      return false;
    return true;
  }
  
  @Override
  @Pure
  public int hashCode() {
    return 31 * 1 + this.frameId;
  }
}
