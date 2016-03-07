/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package netdecoder;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author edroaldo
 */
public class Path {
    List<Edge> path = new ArrayList<Edge>();
    double cost = 0.0;
    double flow = 0.0;

    public Path(List<Edge> path) {
        this.path = path;
        this.cost = getCost();
        this.flow = getFlow();
    }
    
    public List<Edge> getPath() {
        return path;
    }

    public double getCost() {
        double cost = 0.0;
        for (Edge e : path) {
            cost += e.getCost();
        }
        return cost;
    }
    /*@Override
    public String toString(){
        String p = "";
        int i = 0;
        for(Edge e:path){
            if(i < path.size() - 1){
                p += e + " ";
            }else{
                p += e;
            }
            i++;
        }
        return p;
    }*/
    @Override
    public String toString(){
        String p = "";
        int i = 0;
        for(Edge e:path){
            if(i < path.size() - 1){
                p += e.getFrom().getName() + "->";
            }else{
                p += e;
            }
            i++;
        }
        
        return p;
    }
    
    /*@Override
    public String toString(){
        return path.toString();
    }*/
    
    /*@Override
    public String toString(){
        String p = "";
        int i = 0;
        for(Edge e:path){
            if(i < path.size() - 1){
                if(e.getFrom().getSymbol().equals("s")){
                    String from = e.getFrom().getSymbol().split("\\/")[0];
                    String to = e.getTo().getSymbol().split("\\/")[0];
                    p += from + "->" + to + "->";
                }else{
                    String to = e.getTo().getSymbol().split("\\/")[0];
                    p += to + "->";
                }
            }else{
                p += e.getTo().getSymbol();
            }
            i++;
        }
        return p;
    }*/
    
    public Path reversePath(){
        Stack<Edge> reverseAux = new Stack<Edge>();
        List<Edge> reversePath = new ArrayList<Edge>();
        for(Edge e:path){
            //if(e.getFrom().getName().equals("s")) continue;
            reverseAux.push(e);
        }
        while(!reverseAux.isEmpty()){
            reversePath.add(reverseAux.pop());
        }
        return new Path(reversePath);
    }
    
    public double getFlow(){
        double flow = 0.0;
        for(Edge e:path){
            if(e.getFrom().getName().equals("s") || e.getTo().getName().equals("t")) continue;
            //System.out.println(e + " " + e.getFlow());
            flow += e.getFlow();
        }
        return flow;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Path) {
            Path toCompare = (Path) o;
            return this.path.equals(toCompare.path);
            //return this.to.equals(toCompare.to); 
        }
        return false;
    }

    @Override
    public int hashCode() {
        return path.hashCode();
    }
}
