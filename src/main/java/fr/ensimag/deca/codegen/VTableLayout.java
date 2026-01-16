package fr.ensimag.deca.codegen;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class VTableLayout {
    private final int baseBGOffset;
    private final List<String> methodLabels = new ArrayList<>();
    private final Map<String, Integer> methodOffsets = new HashMap<>();

    public VTableLayout(int baseBGOffset) { this.baseBGOffset = baseBGOffset; }

    public void addMethod(String name, String label) {
        if (!methodOffsets.containsKey(name)) {
            methodOffsets.put(name, methodLabels.size());
            methodLabels.add(label);
        } else {
            methodLabels.set(methodOffsets.get(name), label);
        }
    }

    public int getIndex(String name) { 
        return methodOffsets.get(name); 
    }
    
    public String getLabel(int i) { 
        return methodLabels.get(i); 
    }

    public int getSize() { 
        return methodLabels.size(); 
    }

    public int getGBOffset() { 
        return baseBGOffset; 
    }
    
    public void copyLayout(VTableLayout other) {
        this.methodLabels.addAll(other.methodLabels);
        this.methodOffsets.putAll(other.methodOffsets);
    }
}


// public class VTableLayout {
    
//     private final int baseBGOffset;
//     private final List<String> methodLabels = new ArrayList<>();
//     private final List<String> methodNames = new ArrayList<>();
//     private final Map<String, Integer> methodOffsets = new HashMap<>();
//     private final Map<Integer, String> methodDefiningClasses = new HashMap<>();

//     public VTableLayout(int baseBGOffset) {
//         this.baseBGOffset = baseBGOffset;
//     }

//     public void addMethod(String name, String label, String definingClass) {
//         if (!methodOffsets.containsKey(name)) {
//             int index = methodLabels.size();
//             methodOffsets.put(name, index);
//             methodNames.add(name);
//             methodLabels.add(label);
//             methodDefiningClasses.put(index, definingClass);
//         } else {
//             int index = methodOffsets.get(name);
//             methodLabels.set(index, label); // @override
//             methodDefiningClasses.put(index, definingClass); // update origin
//         }
//     }

//     public String getMethodLabel(int index) {
//         return methodLabels.get(index);
//     }

//     public String getMethodName(int index) {
//         return methodNames.get(index);
//     }

//     public String getMethodDefiningClass(int index) {
//         return methodDefiningClasses.get(index);
//     }


//     public int getSize() {
//         return methodLabels.size();
//     }

//     public int getGBOffset() {
//         return baseBGOffset;
//     }

//     public void copyLayout(VTableLayout other) {
//         for (int i = 0; i < other.getSize(); i++) {
//             this.addMethod(
//                 other.methodNames.get(i), 
//                 other.methodLabels.get(i), 
//                 other.methodDefiningClasses.get(i)
//             );
//         }
//     }


// }
