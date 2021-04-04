package com.guymichael.kotlinreact;

import androidx.annotation.Nullable;

import com.guymichael.kotlinreact.model.OwnState;

import java.util.List;

//TODO move to Kotlin, carefully
public class Utils {

    private final static boolean strictMode = BuildConfig.DEBUG;


    public static boolean shallowEquality(@Nullable OwnState first, @Nullable OwnState second) throws IllegalArgumentException {
        if (first == null || second == null) {
            return first == null && second == null;
        }

        if (first == second) {
            return true;
        }

        //in order to allow complex props (not just primitives), we must do actual equality.
        //for primitives, equality is natural. For just any other Object, equals&hashCode have to be implemented correctly
        List<?> firstMembers = first.getAllMembers();
        List<?> secondMembers = second.getAllMembers();

        if (firstMembers.hashCode() != secondMembers.hashCode()) {
            return false;
        }

        if (strictMode) {
            boolean equal = firstMembers.equals(secondMembers);
            if( !equal) {

                Logger.e(Utils.class, "ownState/props have same hashcode but are not equal:\n"
                    + first.toString() + "\n\n"
                    + second.toString()
                );

                return false;
            }
        }

        return true;
    }

    public static int computeHashCode(Object o, @Nullable Object... members) {
        if (o == null) {return -1; }

        // Start with a non-zero constant.
        int result = 17;

        result = 31 * result + o.getClass().getName().hashCode();

        if (members != null) {
            for (@Nullable Object member :members) {
                if (member != null) {
                    result = 31 * result + member.hashCode();
                }
            }
        }

        return result;
    }
}