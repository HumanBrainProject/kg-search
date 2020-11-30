package eu.ebrains.kg.search.api;

import eu.ebrains.kg.search.controller.labels.LabelsController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RequestMapping("/labels")
@RestController
public class Labels {

    private final LabelsController labelsController;

    public Labels(LabelsController labelsController) {
        this.labelsController = labelsController;
    }

    @GetMapping
    public Map<String, Object> getLabels() {
        return labelsController.generateLabels();
    }
}
