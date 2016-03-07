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
public class Queue<Item> {
    private Node first;
    private Node last;
    private int N;
    
    private class Node {
        Item item;
        Node next;
    }
    
    public boolean isEmpty(){
        return first == null;
    }
    
    public int size(){
        return N;
    }
    
    public void enqueue(Item item){
        Node oldlast = last;
        last = new Node();
        last.item = item;
        last.next = null;
        if(isEmpty()){
            first = last;
        }else{
            oldlast.next = last;
        }
        N++;
    }
    
    public Item dequeue(){
        Item item = first.item;
        first = first.next;
        if(isEmpty()){
            last = null;
        }
        N--;
        return item;
    }
    
    public static void main(String args[]){
        Queue<String> queue = new Queue<String>();
        queue.enqueue("Edroaldo");
        queue.enqueue("Angel");
        System.out.println(queue.size());
        System.out.println(queue.dequeue());
        System.out.println(queue.dequeue());
        System.out.println(queue.size());
    }
    
}
