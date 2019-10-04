package akashihi.osm.parallelpbf.entity;

import lombok.Data;

import java.util.List;

@Data
public class Header {
    final List<String> requiredFeatures;
    final List<String> optionalFeatures;
    String writingProgram;
    String source;
}
