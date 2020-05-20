package cn.edu.thssdb.predicate.base;

/**
 * The base class for all kinds of operators.
 * We use visitor pattern to allow different operations on the predicate tree,
 * including: table binding, optimization/simplification, evaluation
 */
public interface Predicate {
    void accept(PredicateVisitor predicateVisitor);
}
