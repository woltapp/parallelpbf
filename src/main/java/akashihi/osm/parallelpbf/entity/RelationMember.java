package akashihi.osm.parallelpbf.entity;

import lombok.Data;

@Data
public class RelationMember {
    public enum Type {
        NODE(0),
        WAY(1),
        RELATION(2);

        private int value;

        Type(int v) {
            this.value = v;
        }

        public static Type get(int v) {
            for (Type t : Type.values()) {
                if (t.value == v) {
                    return t;
                }
            }
            throw new IllegalArgumentException();
        }
    }

    final private Long id;
    final private String role;
    final Type type;
}
