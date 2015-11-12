public class BST {
	private Bits data;
	private BST parent;
	private BST left;
	private BST right;


	/**
	 * Returns a new, uninitialized BST object.
	 **/
	public BST() {
		parent = left = right = null;
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
	 * Determines if the BST has been initialized
	 *
	 * @return	True if tree is uninitialized
	 **/
	public boolean isEmpty() {
		return data == null;
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