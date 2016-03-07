/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author edroaldo
 */
public class NodeCategory implements Serializable{
    private String category;
    private Set<String> proteins;
    private List<EdgeCategory> edges;
    private int numProteins;
    
    public NodeCategory(String category){
        this.category = category;
        this.proteins = new LinkedHashSet<String>();
        this.edges = new ArrayList<EdgeCategory>();
    }

    @Override
    public boolean equals(Object o){
        if(o instanceof Node){
            NodeCategory toCompare = (NodeCategory)o;
            return this.category.equals(toCompare.category); 
        }
        return false;
    }
    /**
     * @return the category
     */
    public String getCategory() {
        return category;
    }

    /**
     * @param category the category to set
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * @return the proteins
     */
    public Set<String> getProteins() {
        return proteins;
    }

    /**
     * @param proteins the proteins to set
     */
    public void setProteins(Set<String> proteins) {
        this.proteins = proteins;
    }

    /**
     * @return the edges
     */
    public List<EdgeCategory> getEdges() {
        return edges;
    }

    /**
     * @param edges the edges to set
     */
    public void setEdges(List<EdgeCategory> edges) {
        this.edges = edges;
    }

    /**
     * @return the numProteins
     */
    public int getNumProteins() {
        return numProteins;
    }

    /**
     * @param numProteins the numProteins to set
     */
    public void setNumProteins(int numProteins) {
        this.numProteins = numProteins;
    }
    
}
