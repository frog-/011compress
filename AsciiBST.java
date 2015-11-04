/**
 * A basic binary search tree, specifically for Ascii objects.
 *
 * Use update() to start the tree, add to the tree, or update the tree. The one
 * method handles all cases.
 *
 * Why does this not extend BinaryTree? In order to make BinaryTree work with
 * the update() and find() methods, the generic type _must_ implement the 
 * Comparable interface, which would require me to override basically every 
 * method anyways, or to make BinaryTree less generic. Both are unfavourable.
 *
 * Why is this not generic? If it weren't, calls to Ascii object methods would
 * have to be done separately while updating, which would be anti-encapsulation
 **/
import java.util.PriorityQueue;
import java.util.Comparator;
import java.io.*;

public class AsciiBST {
	private Ascii data;
	private AsciiBST parent;
	private AsciiBST left;
	private AsciiBST right;
	private static PriorityQueue<Ascii> queue;

	public AsciiBST() {
		parent = left = right = null;
		queue = new PriorityQueue<Ascii>(26, new FreqComparator());
		data = null;
	}


	/**
	 * Gives the tree its first node.
	 *
	 * @param	data	Dummy Ascii object to store as key
	 **/
	private void makeRoot(Ascii data) {
		this.data = data;
		data.addInstance();
	}


	/**
	 * Updates an existing Ascii object, or adds the object if it can't be
	 * found, including if the tree is empty.
	 *
	 * @param	val	A dummy Ascii object initialized with the search key
	 **/
	public void update(Ascii val) {
		AsciiBST lastLeaf = find(val);

		//If tree is empty, add as root
		if (lastLeaf == null) {
			makeRoot(val);

		//If value already exists, update
		} else if (lastLeaf.getData().equals(val)) {
			lastLeaf.getData().addInstance();

		//If value is not in tree, add to tree
		} else {
			AsciiBST newLeaf = new AsciiBST();
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
	public AsciiBST find(Ascii val) {
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
	 * Adds all Ascii objects in the given AsciiBST tree to a priority queue
	 * based on frequency in the text, least frequent occurring at the front
	 * of the queue.
	 *
	 * @param	tree	The root node of the tree to sort
	 * @return	The processed PriorityQueue
	 **/
	public static PriorityQueue<Ascii> queueByFrequency(AsciiBST tree) {
		if (tree != null)
		{
			queue.add(tree.getData());
			queueByFrequency(tree.getLeft());
			queueByFrequency(tree.getRight());
		}

		return queue;
	}


	/**
	 * Add EOF code
	 **/
	public void finalize() {
		Ascii eof = new Ascii('\0');
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
	private class FreqComparator implements Comparator<Ascii> {

		@Override
		public int compare(Ascii letter1, Ascii letter2) {
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
	public static void preorder(AsciiBST tree)
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
	public Ascii getData() {
		return data;
	}

	public void setData(Ascii data) {
		this.data = data;
	}

	public AsciiBST getLeft() {
		return left;
	}

	public void setLeft(AsciiBST tree) {
		left = tree;
	}

	public AsciiBST getRight() {
		return right;
	}

	public void setRight(AsciiBST tree) {
		right = tree;
	}
}