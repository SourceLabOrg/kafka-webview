/**
 * MIT License
 *
 * Copyright (c) 2017, 2018, 2019 SourceLab.org (https://github.com/SourceLabOrg/kafka-webview/)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package org.sourcelab.kafka.webview.ui.controller.configuration.producer;

import org.sourcelab.kafka.webview.ui.controller.BaseController;
import org.sourcelab.kafka.webview.ui.controller.configuration.producer.forms.ProducerForm;
import org.sourcelab.kafka.webview.ui.controller.configuration.view.forms.ViewForm;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperations;
import org.sourcelab.kafka.webview.ui.manager.kafka.KafkaOperationsFactory;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicDetails;
import org.sourcelab.kafka.webview.ui.manager.kafka.dto.TopicList;
import org.sourcelab.kafka.webview.ui.manager.ui.BreadCrumbManager;
import org.sourcelab.kafka.webview.ui.manager.ui.FlashMessage;
import org.sourcelab.kafka.webview.ui.model.Cluster;
import org.sourcelab.kafka.webview.ui.model.Producer;
import org.sourcelab.kafka.webview.ui.model.ProducerMessage;
import org.sourcelab.kafka.webview.ui.repository.ClusterRepository;
import org.sourcelab.kafka.webview.ui.repository.MessageFormatRepository;
import org.sourcelab.kafka.webview.ui.repository.ProducerMessageRepository;
import org.sourcelab.kafka.webview.ui.repository.ProducerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.*;

/**
 *  Controller for CRUD over Producer entities.
 */
@Controller
@RequestMapping("/configuration/producer")
public class ProducerConfigController extends BaseController
{
    @Autowired
    private ClusterRepository clusterRepository;

    @Autowired
    private MessageFormatRepository messageFormatRepository;

    @Autowired
    private ProducerRepository producerRepository;

    @Autowired
    private ProducerMessageRepository producerMessageRepository;

    @Autowired
    private KafkaOperationsFactory kafkaOperationsFactory;


    @GetMapping
    public String index(final Model model) {
        // Setup breadcrumbs
        setupBreadCrumbs(model, null, null);

        // Retrieve all message formats
        final Iterable<Producer> producerList = producerRepository.findAllByOrderByNameAsc();
        model.addAttribute("producers", producerList);

        return "configuration/producer/index";
    }

    /**
     * GET Displays create producer form.
     */
    @GetMapping( "/create")
    public String createProducerForm( final ProducerForm producerForm, final Model model)
    {
        // Setup breadcrubs
        if(!model.containsAttribute( "BreadCrumbs" ))
        {
            setupBreadCrumbs( model, "Create", null );
        }

        // Retrieve all clusters
        model.addAttribute("clusters", clusterRepository.findAllByOrderByNameAsc());

        // Retrieve all message formats
        model.addAttribute("defaultMessageFormats", messageFormatRepository.findByIsDefaultOrderByNameAsc(true));
        model.addAttribute("customMessageFormats", messageFormatRepository.findByIsDefaultOrderByNameAsc(false));

        model.addAttribute("topics", new ArrayList<>());

        if( producerForm.getClusterId() != null )
        {
            clusterRepository.findById( producerForm.getClusterId() ).ifPresent( (cluster) -> {
                try (final KafkaOperations operations = kafkaOperationsFactory.create(cluster, getLoggedInUserId())) {
                    final TopicList topics = operations.getAvailableTopics();
                    model.addAttribute("topics", topics.getTopics());

                    // If we have a selected topic
                    if (producerForm.getTopic() != null && !"!".equals(producerForm.getTopic())) {
                        final TopicDetails topicDetails = operations.getTopicDetails(producerForm.getTopic());
                        model.addAttribute("partitions", topicDetails.getPartitions());
                    }
                }
            } );
        }

        return "configuration/producer/create";
    }

    @GetMapping("/edit/{id}")
    public String editProducer(@PathVariable final Long id, final ProducerForm producerForm,
        final RedirectAttributes redirectAttributes,
        final Model model)
    {
        final Optional<Producer> producerOptional = producerRepository.findById( id );
        if(!producerOptional.isPresent())
        {
            // Set flash message
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find producer!"));

            // redirect to producer index
            return "redirect:/configuration/producer";
        }
        final Producer producer = producerOptional.get();

        setupBreadCrumbs( model, "Edit: " + producer.getName(), null );

        producerForm.setId( producer.getId() );
        producerForm.setClusterId( producer.getCluster().getId() );
        producerForm.setName( producer.getName() );
        producerForm.setProducerMessageClassName( producer.getProducerMessage().getQualifiedClassName() );
        producerForm.setProducerMessagePropertyNameList( producer.getProducerMessage().getPropertyNameList() );

        return createProducerForm( producerForm, model );
    }

    @RequestMapping(path = "/delete/{id}", method = RequestMethod.POST)
    public String deleteProducer(@PathVariable final Long id, final RedirectAttributes redirectAttributes) {
        // Retrieve it
        if (!producerRepository.existsById(id)) {
            // Set flash message & redirect
            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find producer!"));
        } else {
            // Delete it
            producerRepository.deleteById(id);

            redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess("Deleted producer!"));
        }

        // redirect to cluster index
        return "redirect:/configuration/producer";
    }

    @RequestMapping(path = "/update", method = RequestMethod.POST)
    public String updateProducer(
        @Valid final ProducerForm producerForm,
        final BindingResult bindingResult,
        final RedirectAttributes redirectAttributes,
        final Model model)
    {
        // Determine if we're updating or creating
        final boolean updateExisting = producerForm.exists();

        // Ensure that producer name is not already used.
        final Producer existingProducer = producerRepository.findByName(producerForm.getName());
        if (existingProducer != null) {
            // If we're updating, exclude our own id.
            if (!updateExisting
                || !producerForm.getId().equals(existingProducer.getId())) {
                bindingResult.addError(new FieldError(
                    "producerForm", "name", producerForm.getName(), true, null, null, "Name is already used")
                );
            }
        }

        // If we have errors
        if (bindingResult.hasErrors()) {
            return createProducerForm(producerForm, model);
        }

        // If we're updating
        final Producer producer;
        final ProducerMessage producerMessage;
        final String successMessage;
        if (updateExisting) {
            // Retrieve producer
            final Optional<Producer> producerOptional = producerRepository.findById(producerForm.getId());
            if (!producerOptional.isPresent()) {
                // Set flash message and redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find producer!"));

                // redirect to producer index
                return "redirect:/configuration/producer";
            }
            producer = producerOptional.get();

            //Retrieve producer message
            final Optional<ProducerMessage> producerMessageOptional = producerMessageRepository.findByProducer( producer );
            if(!producerMessageOptional.isPresent())
            {
                // Set flash message and redirect
                redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newWarning("Unable to find producer's message!"));

                //redirect to producer index
                return "redirect:/configuration/producer";
            }
            producerMessage = producerMessageOptional.get();

            successMessage = "Updated producer successfully!";
        } else {
            producer = new Producer();
            producerMessage = new ProducerMessage();
            producer.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            producerMessage.setCreatedAt( new Timestamp( System.currentTimeMillis() ) );
            successMessage = "Created new producer!";
        }

        // Update properties
//        TODO uncomment when we want to send more than a map of string/string as a kafka message
//        final MessageFormat keyMessageFormat = messageFormatRepository.findById(producerForm.getKeyMessageFormatId()).get();
//        final MessageFormat valueMessageFormat = messageFormatRepository.findById(producerForm.getValueMessageFormatId()).get();
        final Cluster cluster = clusterRepository.findById(producerForm.getClusterId()).get();

        producer.setName( producerForm.getName() );
        producer.setTopic( producerForm.getTopic() );
//        TODO uncomment when we want to send more than a map of string/string as a kafka message
//        producer.setKeyMessageFormat( keyMessageFormat );
//        producer.setValueMessageFormat( valueMessageFormat );
        producer.setCluster( cluster );

        producerMessage.setName( producer.getName() + "Message" );
        producerMessage.setQualifiedClassName( producerForm.getProducerMessageClassName() );
        producerMessage.setProducer( producer );
        producerMessage.setPropertyNameList( producerForm.getProducerMessagePropertyNameList() );

        // Persist the producer
        producer.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        producerRepository.save(producer);

        // Persist the producer's message
        producerMessage.setUpdatedAt( new Timestamp( System.currentTimeMillis() ) );
        producerMessageRepository.save( producerMessage );


        // Set flash message
        redirectAttributes.addFlashAttribute("FlashMessage", FlashMessage.newSuccess(successMessage));

        // redirect to cluster index
        return "redirect:/configuration/producer";

    }

    private void setupBreadCrumbs(final Model model, String name, String url) {
        // Setup breadcrumbs
        final BreadCrumbManager manager = new BreadCrumbManager(model)
            .addCrumb("Configuration", "/configuration");

        if (name != null) {
            manager.addCrumb("Producers", "/configuration/producer");
            manager.addCrumb(name, url);
        } else {
            manager.addCrumb("Producers", null);
        }
    }

}
