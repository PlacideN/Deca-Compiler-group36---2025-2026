package fr.ensimag.deca.context;

import java.util.ArrayList;
import java.util.List;

/**
 * Signature of a method (i.e. list of arguments)
 *
 * @author gl36
 * @date 01/01/2026
 */
public class Signature {
    List<Type> args = new ArrayList<Type>();

    public void add(Type t) {
        args.add(t);
    }
    
    public Type paramNumber(int n) {
        return args.get(n);
    }
    
    public int size() {
        return args.size();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Signature) {
            Signature other = (Signature) o;
            if (this.args.size() != other.args.size()) {
                return false;
            }
            for (int i = 0; i < this.size(); i++) {
                // each instance type is unique in the compiler so we can use equals 
                // to compare references only without overriding the method in Type
                if (!this.args.get(i).equals(other.args.get(i))) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return args.hashCode();
    }

}
