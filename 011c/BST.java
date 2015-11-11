import java.util.PriorityQueue;
import java.util.Comparator;

public class BST {
	private Bits data;
	private BST parent;
	private BST left;
	private BST right;
	//This shouldn't exist, it was just easiest at the time
	private static PriorityQueue<Bits> queue;


	/**
	 * Returns a new, uninitialized BST object.
	 **/
	public BST() {
		parent = left = right = null;
		queue = new PriorityQueue<Bits>(257, new FreqComparator());
		data = null;
	}


	/**
	 * Gives the tree its first node.
	 *
	 * @param	data	Dummy Bits object to store as key
	 **/
	private void makeRoot(Bits data) {
		this.data = data;

		//Set the count to 1.
		data.addInstance();
	}


	/**
	 * Updates an existing Bits object, or adds the object to the tree if it 
	 * can't be found, including if the tree is empty.
	 *
	 * There is no "add" method, and outside code has no reason to manually set
	 * roots, so all operations which involve adding info to the tree are
	 * handled through update().
	 *
	 * @param	val	A dummy Bits object initialized with the search key
	 **/
	public void update(Bits val) {
		//Search for 'val' in the BST
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

			//Add left or right, depending on parent's value
			if (lastLeaf.getData().compareTo(val) < 0) {
				lastLeaf.setRight(newLeaf);
			} else {
				lastLeaf.setLeft(newLeaf);
			}
		}
	}


	/**
	 * Finds a BST object that contains the argument. If the argument 
	 * is not found in the tree, returns the BST that would be the parent,
	 * or NULL if the tree is empty.
	 *
	 * @param	val	A dummy Bits object initialized with the search key
	 * @return 	Reference to BST object containing specified key
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
		if (tree != null) {
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
	 * Adds EOF object, so the compressed file can have a clear end.
	 *
	 * This should be called after the input file has been read, but before
	 * the Huffman codes are generated.
	 **/
	public void finalize() {
		/*
		 * The value is written to file as 4bits so that there can be no byte-
		 * collision.
		 */
		Bits eof = new Bits("0000");
		update(eof);

		/*
		 * Set the probability to zero to force the null byte to the top. At
		 * the moment this is ABSOLUTELY NECESSARY because the decompressor
		 * tries to read the null byte first.
		 */
		eof.setNullByte();
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
	 * Comparator to allow sorting by frequency. Less frequent items should
	 * appear "before" more frequent items.
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
	 * Traverses the tree in a Root-Left-Right pattern.
	 *
	 * This is only here for debugging purposes.
	 **/
	public static void preorder(BST tree)
	{
		if (tree != null) {
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