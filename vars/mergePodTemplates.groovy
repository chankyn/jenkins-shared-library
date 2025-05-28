import org.csanchez.jenkins.plugins.kubernetes.PodTemplate  
import org.csanchez.jenkins.plugins.kubernetes.pod.yaml.Merge
import static org.csanchez.jenkins.plugins.kubernetes.PodTemplateUtils.combine
import org.csanchez.jenkins.plugins.kubernetes.PodTemplateBuilder
import org.csanchez.jenkins.plugins.kubernetes.KubernetesSlave
import org.csanchez.jenkins.plugins.kubernetes.KubernetesCloud
import io.fabric8.kubernetes.api.model.Pod
import io.fabric8.kubernetes.client.utils.Serialization

def addYamlExt(String string)  {
    return string.endsWith(".yaml") ? string : string + ".yaml"
}

def call(String yamlFiles){

    String[] yamlFilesSplit = yamlFiles.split(',')
    println "yamlFiles: "+yamlFilesSplit
    String parentYaml = addYamlExt(yamlFilesSplit[0])
    println "ParentFile: "+parentYaml
    writeFile("")
    parentYaml = libraryResource(parentYaml)
    PodTemplate parent = new PodTemplate()
    parent.setYaml(parentYaml)

    for (i = 1; i < yamlFilesSplit.size(); i++) {
        String childYaml = addYamlExt(yamlFilesSplit[i])
        println "ChildFile: "+childYaml
        childYaml = libraryResource(childYaml)

        println "Parent ---\n" + parent
        println "ChildYaml ---\n" + childYaml
        
        PodTemplate child = new PodTemplate()
        child.setYaml(childYaml)
        child.setYamlMergeStrategy(new Merge())
        child.setInheritFrom("parent")

        PodTemplate result = combine(parent, child)
        parent = result
    }
    
    KubernetesCloud cloud = Jenkins.get().getCloud("kubernetes")
    KubernetesSlave ks = new KubernetesSlave.Builder().cloud(cloud).podTemplate(parent).build()
    Pod pod = new PodTemplateBuilder(parent, ks).build()
    String yaml = Serialization.asYaml(pod)
    println "Combined\n" + yaml

    return yaml
}
