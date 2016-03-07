/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.Serializable;

/**
 *
 * @author edroaldo
 */
public class EdgeCategory implements Serializable{
    private NodeCategory from;
    private NodeCategory to;
    private int sharedProteins;
    private int weight;
    
    public EdgeCategory(NodeCategory from, NodeCategory to){
        this.from = from;
        this.to = to;
        this.sharedProteins = -1;
        this.weight = 0;
    }
    
    public String toString() {
        return from.getCategory()+ "->" + to.getCategory() + " " + this.sharedProteins;
    }
    
    /**
     * @return the from
     */
    public NodeCategory getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(NodeCategory from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public NodeCategory getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(NodeCategory to) {
        this.to = to;
    }

    /**
     * @return the sharedProteins
     */
    public int getSharedProteins() {
        return sharedProteins;
    }

    /**
     * @param sharedProteins the sharedProteins to set
     */
    public void setSharedProteins(int sharedProteins) {
        this.sharedProteins = sharedProteins;
    }

    /**
     * @return the weight
     */
    public int getWeight() {
        return weight;
    }

    /**
     * @param weight the weight to set
     */
    public void setWeight(int weight) {
        this.weight = weight;
    }
    @Override
    public int hashCode() {
        return (from.getCategory() + to.getCategory()).hashCode();
    }

    
    @Override
    public boolean equals(Object o){
        if(o instanceof Edge){
            EdgeCategory toCompare = (EdgeCategory)o;
            return this.from.equals(toCompare.from) && this.to.equals(toCompare.to); 
            //return this.to.equals(toCompare.to); 
        }
        return false;
    }
    
}
