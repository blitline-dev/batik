/*

   Copyright 2003,2006  The Apache Software Foundation 

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.

 */
package org.apache.batik.dom.svg;

import java.util.ArrayList;
import java.util.Iterator;

import org.apache.batik.parser.ParseException;

import org.w3c.dom.Attr;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.svg.SVGAnimatedLengthList;
import org.w3c.dom.svg.SVGException;
import org.w3c.dom.svg.SVGLength;
import org.w3c.dom.svg.SVGLengthList;

/**
 * This class is the implementation of the {@link SVGAnimatedLengthList}
 * interface.
 *
 * @author <a href="mailto:nicolas.socheleau@bitflash.com">Nicolas Socheleau</a>
 * @version $Id$
 */
public class SVGOMAnimatedLengthList 
    extends AbstractSVGAnimatedValue
    implements SVGAnimatedLengthList {

    /**
     * The base value.
     */
    protected BaseSVGLengthList baseVal;

    /**
     * The animated value.
     */
    protected AnimSVGLengthList animVal;

    /**
     * Whether the list is changing.
     */
    protected boolean changing;

    /**
     * Default value for the length list.
     */
    protected String defaultValue;

    /**
     * Whether empty length lists are allowed.
     */
    protected boolean emptyAllowed;

    /**
     * The direction of the lengths in this list.
     */
    protected short direction;

    /**
     * Creates a new SVGOMAnimatedLengthList.
     * @param elt The associated element.
     * @param ns The attribute's namespace URI.
     * @param ln The attribute's local name.
     * @param defaultValue The default value if the attribute is not specified.
     * @param emptyAllowed Whether a list with no items is allowed.
     * @param direction The direction of the lengths in the list.
     */
    public SVGOMAnimatedLengthList(AbstractElement elt,
                                   String ns,
                                   String ln,
                                   String defaultValue,
                                   boolean emptyAllowed,
                                   short direction) {
        super(elt, ns, ln);
        this.defaultValue = defaultValue;
        this.emptyAllowed = emptyAllowed;
        this.direction = direction;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedLengthList#getBaseVal()}.
     */
    public SVGLengthList getBaseVal() {
        if (baseVal == null) {
            baseVal = new BaseSVGLengthList();
        }
        return baseVal;
    }

    /**
     * <b>DOM</b>: Implements {@link SVGAnimatedLengthList#getAnimVal()}.
     */
    public SVGLengthList getAnimVal() {
        if (animVal == null) {
            animVal = new AnimSVGLengthList();
        }
        return animVal;
    }

    /**
     * Sets the animated value.
     */
    public void setAnimatedValue(short[] types, float[] values) {
        if (animVal == null) {
            animVal = new AnimSVGLengthList();
        }
        hasAnimVal = true;
        animVal.setAnimatedValue(types, values);
        fireAnimatedAttributeListeners();
    }

    /**
     * Resets the animated value.
     */
    public void resetAnimatedValue() {
        hasAnimVal = false;
        fireAnimatedAttributeListeners();
    }

    /**
     * Called when an Attr node has been added.
     */
    public void attrAdded(Attr node, String newv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        // XXX Notify baseVal listeners (if we need them).
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * Called when an Attr node has been modified.
     */
    public void attrModified(Attr node, String oldv, String newv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        // XXX Notify baseVal listeners (if we need them).
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }

    /**
     * Called when an Attr node has been removed.
     */
    public void attrRemoved(Attr node, String oldv) {
        if (!changing && baseVal != null) {
            baseVal.invalidate();
        }
        // XXX Notify baseVal listeners (if we need them).
        if (!hasAnimVal) {
            fireAnimatedAttributeListeners();
        }
    }
    
    /**
     * {@link SVGLengthList} implementation for the base length list value.
     */
    public class BaseSVGLengthList extends AbstractSVGLengthList {

        /**
         * Creates a new BaseSVGLengthList.
         */
        public BaseSVGLengthList() {
            super(SVGOMAnimatedLengthList.this.direction);
        }

        /**
         * Create a DOMException.
         */
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }

        /**
         * Create a SVGException.
         */
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {

            return ((SVGOMElement)element).createSVGException(type, key, args);
        }

        /**
         * Returns the element owning the attribute with which this length
         * list is associated.
         */
        protected Element getElement() {
            return element;
        }

        /**
         * Returns the value of the DOM attribute containing the length list.
         */
        protected String getValueAsString() {
            Attr attr = element.getAttributeNodeNS(namespaceURI, localName);
            if (attr == null) {
                return defaultValue;
            }
            return attr.getValue();
        }

        /**
         * Sets the DOM attribute value containing the length list.
         */
        protected void setAttributeValue(String value) {
            try {
                changing = true;
                element.setAttributeNS(namespaceURI, localName, value);
            } finally {
                changing = false;
            }
        }

        /**
         * Initializes the list, if needed.
         */
        protected void revalidate() {
            if (valid) {
                return;
            }

            String s = getValueAsString();
            boolean isEmpty = s != null && s.length() == 0;
            if (s == null || isEmpty && !emptyAllowed) {
                throw new LiveAttributeException(element, localName, true,
                                                 null);
            }
            if (isEmpty) {
                itemList = new ArrayList(1);
            } else {
                try {
                    ListBuilder builder = new ListBuilder();

                    doParse(s, builder);

                    if (builder.getList() != null) {
                        clear(itemList);
                    }
                    itemList = builder.getList();
                } catch (ParseException e) {
                    itemList = new ArrayList(1);
                    valid = true;
                    throw new LiveAttributeException(element, localName, false,
                                                     s);
                }
            }
            valid = true;
        }
    }

    /**
     * {@link SVGLengthList} implementation for the animated length list value.
     */
    protected class AnimSVGLengthList extends AbstractSVGLengthList {

        /**
         * Creates a new AnimSVGLengthList.
         */
        public AnimSVGLengthList() {
            super(SVGOMAnimatedLengthList.this.direction);
            itemList = new ArrayList(1);
        }

        /**
         * Create a DOMException.
         */
        protected DOMException createDOMException(short type, String key,
                                                  Object[] args) {
            return element.createDOMException(type, key, args);
        }

        /**
         * Create a SVGException.
         */
        protected SVGException createSVGException(short type, String key,
                                                  Object[] args) {

            return ((SVGOMElement)element).createSVGException(type, key, args);
        }

        /**
         * Returns the element owning this SVGLengthList.
         */
        protected Element getElement() {
            return element;
        }

        /**
         * <b>DOM</b>: Implements {@link SVGLengthList#getNumberOfItems()}.
         */
        public int getNumberOfItems() {
            if (hasAnimVal) {
                return super.getNumberOfItems();
            }
            return getBaseVal().getNumberOfItems();
        }

        /**
         * <b>DOM</b>: Implements {@link SVGLengthList#getItem(int)}.
         */
        public SVGLength getItem(int index) throws DOMException {
            if (hasAnimVal) {
                return super.getItem(index);
            }
            return getBaseVal().getItem(index);
        }

        /**
         * Returns the value of the DOM attribute containing the point list.
         */
        protected String getValueAsString() {
            if (itemList.size() == 0) {
                return "";
            }
            StringBuffer sb = new StringBuffer();
            Iterator i = itemList.iterator();
            if (i.hasNext()) {
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            while (i.hasNext()) {
                sb.append(getItemSeparator());
                sb.append(((SVGItem) i.next()).getValueAsString());
            }
            return sb.toString();
        }

        /**
         * Sets the DOM attribute value containing the point list.
         */
        protected void setAttributeValue(String value) {
        }

        /**
         * <b>DOM</b>: Implements {@link SVGLengthList#clear()}.
         */
        public void clear() throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGLengthList#initialize(SVGLength)}.
         */
        public SVGLength initialize(SVGLength newItem)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link
         * SVGLengthList#insertItemBefore(SVGLength, int)}.
         */
        public SVGLength insertItemBefore(SVGLength newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link
         * SVGLengthList#replaceItem(SVGLength, int)}.
         */
        public SVGLength replaceItem(SVGLength newItem, int index)
                throws DOMException, SVGException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGLengthList#removeItem(int)}.
         */
        public SVGLength removeItem(int index) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }

        /**
         * <b>DOM</b>: Implements {@link SVGLengthList#appendItem(SVGLength)}.
         */
        public SVGLength appendItem(SVGLength newItem) throws DOMException {
            throw element.createDOMException
                (DOMException.NO_MODIFICATION_ALLOWED_ERR,
                 "readonly.length.list", null);
        }

        /**
         * Sets the animated value.
         */
        protected void setAnimatedValue(short[] types, float[] values) {
            int size = itemList.size();
            int i = 0;
            while (i < size && i < types.length) {
                SVGLengthItem l = (SVGLengthItem) itemList.get(i);
                l.unitType = types[i];
                l.value = values[i];
                l.direction = direction;
                i++;
            }
            while (i < types.length) {
                appendItemImpl(new SVGLengthItem(types[i], values[i],
                                                 direction));
                i++;
            }
            while (size > types.length) {
                removeItemImpl(--size);
            }
        }

        /**
         * Resets the value of the associated attribute.  Does nothing, since
         * there is no attribute for an animated value.
         */
        protected void resetAttribute() {
        }

        /**
         * Resets the value of the associated attribute.  Does nothing, since
         * there is no attribute for an animated value.
         */
        protected void resetAttribute(SVGItem item) {
        }

        /**
         * Initializes the list, if needed.  Does nothing, since there is no
         * attribute to read the list from.
         */
        protected void revalidate() {
            valid = true;
        }
    }
}
