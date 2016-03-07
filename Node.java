/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package netdecoder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author edroaldo
 */
public class Node implements Serializable{
    private String name;
    private String sgd;
    private String symbol;
    private int id;
    static int nextId = 0;
    private boolean visited;
    
    private ArrayList<Edge> edges;
    private ArrayList<String> geneOntology;
    private ArrayList<String> currentGOs;
    private Set<String> outflowGOs;
    private ArrayList<String> functionalContext;
    private Map<String, Set<String>> outflowGOmap;
    private Set<String> functionalCategory;
    private double expression;
    
    public Node(String name, int id){
        this.setName(name);
        this.setId(id);
    }
    public Node(String name){
        this.name = name;
        edges = new ArrayList<Edge>();
        geneOntology = new ArrayList<String>();
        currentGOs = new ArrayList<String>();
        outflowGOs = new LinkedHashSet<String>();
        outflowGOmap = new LinkedHashMap<String, Set<String>>();
        functionalContext = new ArrayList<String>();
        functionalCategory = new LinkedHashSet<String>();
        visited = false;
        expression = 0.0;
        //id = nextId++;
    }
    
    public Node copyNode(){
        Node node = new Node(this.getName());
        node.name = this.getName();
        node.symbol = this.getSymbol();
        node.geneOntology = new ArrayList<String>();
        node.currentGOs = new ArrayList<String>();
        node.outflowGOs = new LinkedHashSet<String>();
        node.outflowGOmap = new LinkedHashMap<String, Set<String>>();
        node.functionalContext = new ArrayList<String>();
        node.functionalCategory = new LinkedHashSet<String>();
        node.visited = false;
        node.expression = 0.0;
        
        return node;
    }

    @Override
    public String toString() {
        return this.getName(); //To change body of generated methods, choose Tools | Templates.
    }
    
    public int degree(){
        return this.getEdges().size() / 2;
    }
    public Set<String> getOutflowGOs(){
        return outflowGOs;
    }
    public void setOutflowGOs(Set<String> outflowsGO){
        this.outflowGOs = outflowsGO;
    }
    
    public Iterator<Edge> edges(){
        return edges.iterator();
    }
    
    public void addEdge(Edge edge){
        edges.add(edge);
    }
    
    public List<Node> getFlowNeighbors(Node previous){
        ArrayList<Node> neighFlow = new ArrayList<Node>();
        for(Edge e:this.getEdges()){
            Node u = e.getOther(this);
            if (!u.getName().equals(previous.getName())) {
                if (e.getFlow() != 0.0) {
                    //System.out.println("flow: " + e.getFlow() + " " + u.getSymbol() + " " + e);
                    if (!neighFlow.contains(u)) {
                        neighFlow.add(u);
                    }
                }
            }
        }
        return neighFlow;
    }
    public double getTotalFlow(){
        double flow = 0.0;
        for(Edge e:getEdges()){
            //if(!e.getFrom().getName().equals(this.getName())){
                flow += e.getFlow();
            //}
        }
        return flow;
    }
    public double getOutflow(){
        double flow = 0.0;
        for(Edge e:getEdges()){
            if(e.getFrom().getName().equals(this.getName())){
                System.out.println("Edge: " + e + " Flow: " + e.getFlow());
                flow += e.getFlow();
            }
        }
        return flow;
    }
    public Map<String, Double> getTopOutFlow(){
        //List<Double> flows = new ArrayList();
        //List<Double> topFlow = new ArrayList();
        Map<String, Double> outflows = new LinkedHashMap<String, Double>();
        for(Edge e:getEdges()){
            if(e.getFrom().getName().equals(this.getName())){
                //System.out.println("Edge: " + e + " Flow: " + e.getFlow());
                //flows.add(e.getFlow());
                outflows.put(e.getTo().getName(), e.getFlow());
            }
        }
        /*Collections.sort(flows);
        if(flows.size() > 3){
            topFlow = flows.subList(flows.size() - 3, flows.size());
        }else{
            topFlow = flows;
        }
        return topFlow;*/
        return outflows;
        
    }
    public double getInFlow(){
        double flow = 0.0;
        for(Edge e:getEdges()){
            if(e.getTo().getName().equals(this.getName())){
                System.out.println("Edge: " + e + " Flow: " + e.getFlow());
                flow += e.getFlow();
            }
        }
        return flow;
    }
    
    public Set<Node> getNeighbors(){
        LinkedHashSet<Node> neighbors = new LinkedHashSet<Node>();
        for (Edge e : getEdges()) {
            neighbors.add(e.getOther(this));
        }
        return neighbors;
    }
    public Set<Node> getNeighbors(double confidence){
        LinkedHashSet<Node> neighbors = new LinkedHashSet<Node>();
        for(Edge e:getEdges()){
            if(e.getCapacity() > confidence){
            //if(e.getMiscore() > confidence){
                neighbors.add(e.getOther(this));
            }
        }
        return neighbors;
    }
    public Set<String> getNeighborsBasedOnFlow(double flow){
        LinkedHashSet<String> neighbors = new LinkedHashSet<String>();
        for(Edge e:getEdges()){
            if(e.getFlow() > flow){
            //if(e.getMiscore() > confidence){
                neighbors.add(e.getOther(this).getName());
            }
        }
        return neighbors;
    }
    
    public Set<String> getGOContext(Map<String, Node> network, double confidence){
        Set<String> goContext = new LinkedHashSet<String>();
        Set<Node> neighbors = getNeighbors(confidence);
        for(Node n:neighbors){
            //System.out.print(n.getSymbol() + " ");
            List<String> gos = network.get(n.getName()).getGeneOntology();
            if (!gos.isEmpty()) {
                for (String g : gos) {
                    goContext.add(g);
                }
            }
        }
        //System.out.println("");
        //System.out.println(goContext);
        return goContext;
    }
    
    //Utilizando na comparacao de objetos na classe Edge
    @Override
    public boolean equals(Object o){
        if(o instanceof Node){
            Node toCompare = (Node)o;
            return this.name.equals(toCompare.name); 
        }
        return false;
    }
    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the visited
     */
    public boolean isVisited() {
        return visited;
    }

    /**
     * @param visited the visited to set
     */
    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    /**
     * @return the edges
     */
    public ArrayList<Edge> getEdges() {
        return edges;
    }

    /**
     * @param edges the edges to set
     */
    public void setEdges(ArrayList<Edge> edges) {
        this.edges = edges;
    }

    /**
     * @return the sgd
     */
    public String getSgd() {
        return sgd;
    }

    /**
     * @param sgd the sgd to set
     */
    public void setSgd(String sgd) {
        this.sgd = sgd;
    }

    /**
     * @return the symbol
     */
    public String getSymbol() {
        return symbol;
    }

    /**
     * @param symbol the symbol to set
     */
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    /**
     * @return the geneOntology
     */
    public ArrayList<String> getGeneOntology() {
        return geneOntology;
    }

    /**
     * @param geneOntology the geneOntology to set
     */
    public void setGeneOntology(ArrayList<String> geneOntology) {
        this.geneOntology = geneOntology;
    }

    /**
     * @return the currentGOs
     */
    public ArrayList<String> getCurrentGOs() {
        return currentGOs;
    }

    /**
     * @param currentGOs the currentGOs to set
     */
    public void setCurrentGOs(ArrayList<String> currentGOs) {
        this.currentGOs = currentGOs;
    }

    /**
     * @return the functionalContext
     */
    public ArrayList<String> getFunctionalContext() {
        return functionalContext;
    }

    /**
     * @param functionalContext the functionalContext to set
     */
    public void setFunctionalContext(ArrayList<String> functionalContext) {
        this.functionalContext = functionalContext;
    }

    /**
     * @return the outflowGOmap
     */
    public Map<String, Set<String>> getOutflowGOmap() {
        return outflowGOmap;
    }

    /**
     * @param outflowGOmap the outflowGOmap to set
     */
    public void setOutflowGOmap(Map<String, Set<String>> outflowGOmap) {
        this.outflowGOmap = outflowGOmap;
    }

    /**
     * @return the functionalCategory
     */
    public Set<String> getFunctionalCategory() {
        return functionalCategory;
    }

    /**
     * @param functionalCategory the functionalCategory to set
     */
    public void setFunctionalCategory(Set<String> functionalCategory) {
        this.functionalCategory = functionalCategory;
    }

    /**
     * @return the expression
     */
    public double getExpression() {
        return expression;
    }

    /**
     * @param expression the expression to set
     */
    public void setExpression(double expression) {
        this.expression = expression;
    }
}
