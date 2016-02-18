/*

 * Autopsy Forensic Browser
 *
 * Copyright 2015 Basis Technology Corp.
 * Contact: carrier <at> sleuthkit <dot> org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sleuthkit.autopsy.timeline.ui.detailview;

import com.google.common.collect.Iterables;
import java.util.Arrays;
import java.util.Collection;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.action.Action;
import org.controlsfx.control.action.ActionUtils;
import org.sleuthkit.autopsy.coreutils.Logger;
import org.sleuthkit.autopsy.timeline.datamodel.EventCluster;
import org.sleuthkit.autopsy.timeline.datamodel.EventStripe;
import org.sleuthkit.autopsy.timeline.ui.detailview.DetailsChart.HideDescriptionAction;
import static org.sleuthkit.autopsy.timeline.ui.detailview.EventNodeBase.configureActionButton;

/**
 * Node used in {@link EventDetailsChart} to represent an EventStripe.
 */
final public class EventStripeNode extends MultiEventNodeBase<EventStripe, EventCluster, EventClusterNode> {

    private static final Logger LOGGER = Logger.getLogger(EventStripeNode.class.getName());
    private Button hideButton;
    /**
     * Pane that contains EventStripeNodes for any 'subevents' if they are
     * displayed
     *
     * //TODO: move more of the control of subnodes/events here and out of
     * EventDetail Chart
     */
//    private final HBox clustersHBox = new HBox();
    private final ImageView eventTypeImageView = new ImageView();

    @Override
    void installActionButtons() {
        if (hideButton == null) {
            hideButton = ActionUtils.createButton(new HideDescriptionAction(getDescription(), ievent.getDescriptionLoD(), chart),
                    ActionUtils.ActionTextBehavior.HIDE);
            configureActionButton(hideButton);

            infoHBox.getChildren().add(hideButton);
        }
    }

    public EventStripeNode(DetailsChart chart, EventStripe eventStripe, EventClusterNode parentNode) {
        super(chart, eventStripe, parentNode);
        setMinHeight(48);
        //setup description label
        eventTypeImageView.setImage(getEventType().getFXImage());
        descrLabel.setTextOverrun(OverrunStyle.CENTER_ELLIPSIS);
        descrLabel.setGraphic(eventTypeImageView);
        descrLabel.setPrefWidth(USE_COMPUTED_SIZE);
        setAlignment(subNodePane, Pos.BOTTOM_LEFT);

        for (EventCluster cluster : eventStripe.getClusters()) {
            subNodes.add(createChildNode(cluster));
        }

        getChildren().addAll(new VBox(infoHBox, subNodePane));
    }

    @Override
    EventNodeBase<?> createChildNode(EventCluster cluster) {
        if (cluster.getEventIDs().size() == 1) {
            return new SingleEventNode(getChart(), getChart().getController().getEventsModel().getEventById(Iterables.getOnlyElement(cluster.getEventIDs())), this);
        } else {
            return new EventClusterNode(getChart(), cluster, this);
        }
    }

    @Override
    void showHoverControls(final boolean showControls) {
        super.showHoverControls(showControls);
        installActionButtons();
        show(hideButton, showControls);
    }

    public EventStripe getEventStripe() {
        return getEventBundle();
    }

    /**
     * @param w the maximum width the description label should have
     */
    @Override
    public void setMaxDescriptionWidth(double w) {
        descrLabel.setMaxWidth(w);
    }

    /**
     * apply the 'effect' to visually indicate highlighted nodes
     *
     * @param applied true to apply the highlight 'effect', false to remove it
     */
    @Override
    public synchronized void applyHighlightEffect(boolean applied) {
        if (applied) {
            descrLabel.setStyle("-fx-font-weight: bold;"); // NON-NLS
            setBackground(highlightedBackground);
        } else {
            descrLabel.setStyle("-fx-font-weight: normal;"); // NON-NLS
            setBackground(defaultBackground);
        }
    }

    @Override
    void setDescriptionVisibiltiyImpl(DescriptionVisibility descrVis) {
        final int size = getEventStripe().getCount();

        switch (descrVis) {
            case HIDDEN:
                countLabel.setText("");
                descrLabel.setText("");
                break;
            case COUNT_ONLY:
                descrLabel.setText("");
                countLabel.setText(String.valueOf(size));
                break;
            default:
            case SHOWN:
                String description = getEventStripe().getDescription();
                description = parentNode != null
                        ? "    ..." + StringUtils.substringAfter(description, parentNode.getDescription())
                        : description;
                descrLabel.setText(description);
                countLabel.setText(((size == 1) ? "" : " (" + size + ")")); // NON-NLS
                break;
        }
    }

    @Override
    EventHandler<MouseEvent> getDoubleClickHandler() {
        return mouseEvent -> {
        };
    }

    @Override
    Collection<? extends Action> getActions() {
        return Arrays.asList(new HideDescriptionAction(getDescription(), ievent.getDescriptionLoD(), chart));
    }
}
