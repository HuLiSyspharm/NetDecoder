
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
public class Edge implements Serializable,Comparable<Edge>{
    private Node from;
    private Node to;
    private Edge reverse;
    private double capacity;
    private double score;
    private double signScore;
    private double flow;
    private double cost;
    private int path;
    private double difference;
    
    
    public Edge(Node from, Node to){
        this.from = from;
        this.to = to;
        this.flow = 0;
        signScore = 0;
        path = -1;
        difference = 0.0;
        
    }
    public Edge(Node from, Node to, double capacity){
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
        signScore = 0;
        path = -1;
        difference = 0.0;
    }
    
    public Edge(Node from, Node to, double capacity, double cost){
        this.from = from;
        this.to = to;
        this.capacity = capacity;
        this.flow = 0;
        signScore = 0;
        this.cost = cost;
        path = -1;
        difference = 0.0;
    }
    
    public String toString() {
        //return from.getName() + "->" + to.getName() + " " + capacity + "/" + flow ;
        String[] s1 = from.getName().split("\\/");
        String[] s2 = to.getName().split("\\/");
        return s1[0]+ "->" + s2[0];// + ":" + flow;
        //return from.getSymbol()+ "->" + to.getSymbol();// + " ";// + capacity + "/" + flow ;
    }
   
    public double residualCapacityTo(Node node){
        if(node.getName().equals(from.getName())) return flow;
        else if(node.getName().equals(to.getName())) return capacity - flow;
        else throw new IllegalArgumentException("Illegal node");
    }
    
    public void addResidualFlowTo(Node node, double delta){
        if(node.getName().equals(from.getName())) flow -= delta; //backward edge
        else if (node.getName().equals(to.getName())) flow += delta;
    }
    
    public boolean isFrom(Node from){
        if(this.from.equals(from.getName())) return true;
        else return false;
    }
    
    @Override
    public int hashCode() {
        return (from.getName() + to.getName()).hashCode();
    }

    
    @Override
    public boolean equals(Object o){
        if(o instanceof Edge){
            Edge toCompare = (Edge)o;
            return this.from.equals(toCompare.from) && this.to.equals(toCompare.to); 
            //return this.to.equals(toCompare.to); 
        }
        return false;
    }
    
    /*@Override
    public boolean equals(Object o){
        if(o instanceof Edge){
            Edge toCompare = (Edge)o;
            return this.to.equals(toCompare.to); 
        }
        return true;
    }*/
    
    /*public boolean equals(Edge edge){
        if(edge instanceof Edge){
            if((from.getName().equals(edge.getFrom().getName()) && (to.getName().equals(edge.getTo().getName())))){
                return true;
            }
        }
        return false;
    }
    */
    public Node getOther(Node node){
        if(node.getName().equals(this.from.getName())){
            return this.to;
        }else if(node.getName().equals(this.to.getName())){
            return this.from;
        }else{
            throw new IllegalArgumentException("Illegal endpoint");
        }
    }
    /**
     * @return the from
     */
    public Node getFrom() {
        return from;
    }

    /**
     * @param from the from to set
     */
    public void setFrom(Node from) {
        this.from = from;
    }

    /**
     * @return the to
     */
    public Node getTo() {
        return to;
    }

    /**
     * @param to the to to set
     */
    public void setTo(Node to) {
        this.to = to;
    }

    /**
     * @return the capacity
     */
    public double getCapacity() {
        return capacity;
    }

    /**
     * @param capacity the capacity to set
     */
    public void setCapacity(double capacity) {
        this.capacity = capacity;
    }

    /**
     * @return the score
     */
    public double getMiscore() {
        return score;
    }

    /**
     * @param miscore the score to set
     */
    public void setMiscore(double miscore) {
        this.score = miscore;
    }

    /**
     * @return the flow
     */
    public double getFlow() {
        return flow;
    }

    /**
     * @param flow the flow to set
     */
    public void setFlow(double flow) {
        this.flow = flow;
    }

    /**
     * @return the cost
     */
    public double getCost() {
        return cost;
    }

    /**
     * @param cost the cost to set
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    @Override
    public int compareTo(Edge edge) {
        if(this.cost < edge.getCost()) return -1;
        else if(this.cost > edge.getCost()) return +1;
        else return 0;
    }

    /**
     * @return the reverse
     */
    public Edge getReverse() {
        return reverse;
    }

    /**
     * @param reverse the reverse to set
     */
    public void setReverse(Edge reverse) {
        this.reverse = reverse;
    }

    /**
     * @return the path
     */
    public int getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(int path) {
        this.path = path;
    }
    
    /*private void writeObject(final ObjectOutputStream out) throws IOException  {  
      out.writeDouble(this.capacity);  
      out.writeDouble(this.flow);  
      out.writeDouble(this.score);
      out.writeDouble(this.cost);
   } 
    
   private void readObject(final ObjectInputStream in) throws IOException, ClassNotFoundException {  
      this.capacity = in.readDouble();  
      this.flow = in.readDouble();  
      this.score = in.readDouble();  
      this.cost = in.readDouble();
   } */

    /**
     * @return the difference
     */
    public double getDifference() {
        return difference;
    }

    /**
     * @param difference the difference to set
     */
    public void setDifference(double difference) {
        this.difference = difference;
    }

    /**
     * @return the signScore
     */
    public double getSignScore() {
        return signScore;
    }

    /**
     * @param signScore the signScore to set
     */
    public void setSignScore(double signScore) {
        this.signScore = signScore;
    }
}