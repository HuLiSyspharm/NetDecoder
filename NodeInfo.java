/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

/**
 *
 * @author edroaldo
 */
public class NodeInfo {
    private final String previous;
    private boolean forward;
    
    public NodeInfo(String previous, boolean forward){
        this.previous = previous;
        this.forward = forward;
    }
    
    public NodeInfo(String previous){
        this.previous = previous;
        this.forward = true;
    }
    public String toString() {
        return "[previous: " + getPrevious() + ", forward: " + isForward() + "]";
    }
    
    @Override
    public boolean equals(Object o){
        if(o instanceof NodeInfo){
            NodeInfo toCompare = (NodeInfo)o;
            return this.previous.equals(toCompare.previous); 
        }
        return false;
    }


    /**
     * @return the previous
     */
    public String getPrevious() {
        return previous;
    }

    /**
     * @return the forward
     */
    public boolean isForward() {
        return forward;
    }

    /**
     * @param forward the forward to set
     */
    public void setForward(boolean forward) {
        this.forward = forward;
    }
}
