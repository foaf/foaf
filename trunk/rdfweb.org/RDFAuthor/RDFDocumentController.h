/* RDFDocumentController */

#import <Cocoa/Cocoa.h>

@interface RDFDocumentController : NSDocumentController
{
    IBOutlet NSPanel *openUrlPanel;
    IBOutlet NSComboBox *urlComboBox;
}
- (IBAction)openUrl:(id)sender;
- (IBAction)urlSubmitted:(id)sender;
- (IBAction)userCancelled:(id)sender;
@end
