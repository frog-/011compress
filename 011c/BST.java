import java.util.PriorityQueue;
import java.util.Comparator;
import java.io.*;

public class BST {
	private Bits data;
	private BST parent;
	private BST left;
	private BST right;
	private static PriorityQueue<Bits> queue;

	public BST() {
		parent = left = right = null;
		queue = new PriorityQueue<Bits>(16, new FreqComparator());
		data = null;
	}


	/**
	 * Gives the tree its first node.
	 *
	 * @param	data	Dummy Ascii object to store as key
	 **/
	private void makeRoot(Bits data) {
		this.data = data;
		data.addInstance();
	}


	/**
	 * Updates an existing Ascii object, or adds the object if it can't be
	 * found, including if the tree is empty.
	 *
	 * @param	val	A dummy Ascii object initialized with the search key
	 **/
	public void update(Bits val) {
		BST lastLeaf = find(val);

		//If tree is empty, add as root
		if (lastLeaf == null) {
			makeRoot(val);

		//If value already exists, update
		} else if (lastLeaf.getData().equals(val)) {
			lastLeaf.getData().addInstance();

		//If value is not in tree, add to tree
		} else {
			BST newLeaf = new BST();
			newLeaf.setData(val);
			newLeaf.getData().addInstance();
			if (lastLeaf.getData().compareTo(val) < 0) {
				lastLeaf.setRight(newLeaf);
			} else {
				lastLeaf.setLeft(newLeaf);
			}
		}
	}


	/**
	 * Finds an AsciiBST object that contains the argument. If the argument 
	 * is not found in the tree, returns the AsciiBST that would be the parent,
	 * or NULL if the tree is empty.
	 *
	 * @param	val	A dummy Ascii object initialized with the search key
	 * @return 	Reference to AsciiBST object containing specified key
	 **/
	public BST find(Bits val) {
		/*
		 * Only an uninitialized root should be empty. Ensure that all trees
		 * are initialized immediately!
		 */
		if (isEmpty()) {
			return null;
		}

		//Compare the value of this node with the supplied value
		int result = getData().compareTo(val);

		//If this is less than the arg, go right, if possible
		if (result < 0) {
			return (getRight() == null) ? this : getRight().find(val);

		//If this is more than the arg, go left, if possible
		} else if (result > 0) {
			return (getLeft() == null) ? this : getLeft().find(val);

		//If this is the node, return this node
		} else {
			return this;
		}
	}


	/**
	 * Adds all Bits objects in the given BST tree to a priority queue
	 * based on frequency in the text, least frequent occurring at the front
	 * of the queue.
	 *
	 * @param	tree	The root node of the tree to sort
	 * @return	The processed PriorityQueue
	 **/
	public static PriorityQueue<Bits> queueByFrequency(BST tree) {
		if (tree != null)
		{
			//Find final frequency of symbol
			Bits curr = tree.getData();
			curr.calculateProbability();

			//Add symbol to queue and recurse
			queue.add(curr);
			queueByFrequency(tree.getLeft());
			queueByFrequency(tree.getRight());
		}

		return queue;
	}


	/**
	 * Add EOF code
	 **/
	public void finalize() {
		Bits eof = new Bits("0000");
		update(eof);
	}


	/**
	 * Determines if the BST has been initialized
	 *
	 * @return	True if tree is uninitialized
	 **/
	public boolean isEmpty() {
		return data == null;
	}


	/**
	 * Comparator to allow sorting by frequency.
	 **/
	private class FreqComparator implements Comparator<Bits> {

		@Override
		public int compare(Bits letter1, Bits letter2) {
			if (letter1.getProbability() < letter2.getProbability()) {
				return -1;
			} else if (letter1.getProbability() > letter2.getProbability()) {
				return 1;
			} else {
				return 0;
			}
		}
	}


	/**
	 * Preorder traversal of tree
	 **/
	public static void preorder(BST tree)
	{
		if (tree != null)
		{
			System.out.println(tree.getData());
			preorder(tree.getLeft());	
			preorder(tree.getRight());
		}
	}


	/**
	 * Accessors & Mutators
	 **/
	public Bits getData() {
		return data;
	}

	public void setData(Bits data) {
		this.data = data;
	}

	public BST getLeft() {
		return left;
	}

	public void setLeft(BST tree) {
		left = tree;
	}

	public BST getRight() {
		return right;
	}

	public void setRight(BST tree) {
		right = tree;
	}
}